package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class SalisMundusItem extends Item {
    private static final int SWAP_DELAY_TICKS = 50;
    private static final float SOUND_VOLUME = 0.33F;
    private static final float SOUND_PITCH_BASE = 1.0F;
    private static final float SOUND_PITCH_DEVIATION = 0.05F;
    private static final int CONSUMED_COUNT = 1;
    private static final String NO_RESEARCH_KEY = "message.thaumaturge.salis_mundus.no_research";

    public SalisMundusItem(Item.Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void allowUseOnCraftingTable(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) {
            return;
        }
        if (!event.getItemStack().is(TTItems.SALIS_MUNDUS)) {
            return;
        }
        Player player = event.getPlayer();
        if (player != null && player.isCrouching()) {
            event.cancelWithResult(InteractionResult.PASS);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ItemStack held = context.getItemInHand();
        if (!player.mayUseItemAt(pos, face, held)) {
            return InteractionResult.FAIL;
        }
        InteractionHand hand = context.getHand();
        player.swing(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        return applyOnServer(context, serverLevel, serverPlayer, held, pos, face, hand);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    private static InteractionResult applyOnServer(UseOnContext context, ServerLevel level, ServerPlayer player, ItemStack held, BlockPos pos, Direction face, InteractionHand hand) {
        BlockState clicked = level.getBlockState(pos);
        DustTriggerInput input = new DustTriggerInput(held, level, pos, clicked);
        Optional<RecipeHolder<DustTrigger>> match = level.recipeAccess().getRecipeFor(TTRecipeTypes.DUST_TRIGGER.get(), input, level);
        if (match.isEmpty()) {
            return InteractionResult.PASS;
        }
        RecipeHolder<DustTrigger> holder = match.get();
        DustTrigger trigger = holder.value();
        if (!trigger.doesPassGate(player)) {
            Thaumaturge.LOGGER.debug("Salis Mundus trigger {} blocked by research gate {}", holder.id(), trigger.researchGate().orElse(null));
            TTActionBar.sendPurple(player, NO_RESEARCH_KEY);
            return InteractionResult.PASS;
        }
        ItemStack result = trigger.assemble(input);
        if (result.isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemStack consumed = held.copyWithCount(CONSUMED_COUNT);
        if (!player.hasInfiniteMaterials()) {
            held.shrink(CONSUMED_COUNT);
        }
        DustTriggerPlacement placement = null;
        if (trigger.isMultiblock()) {
            placement = trigger.findPlacement(input);
            if (placement == null) {
                return InteractionResult.PASS;
            }
            trigger.execute(input, player, placement, face);
        } else {
            queueSwap(level, pos, clicked, result);
        }
        emitEffects(level, player, hand, pos, context.getClickLocation(), trigger, placement);
        recordProgress(player, holder, result, consumed);
        return InteractionResult.SUCCESS;
    }

    private static void queueSwap(ServerLevel level, BlockPos pos, BlockState original, ItemStack result) {
        if (result.getItem() instanceof BlockItem blockItem) {
            DustTriggerSwapQueue.enqueuePlace(level, pos, original, blockItem.getBlock().defaultBlockState(), SWAP_DELAY_TICKS);
        } else {
            DustTriggerSwapQueue.enqueueDrop(level, pos, original, result, SWAP_DELAY_TICKS);
        }
    }

    private static void emitEffects(ServerLevel level, ServerPlayer player, InteractionHand hand, BlockPos pos, Vec3 start, DustTrigger trigger, @Nullable DustTriggerPlacement placement) {
        DustTriggerFx.emitUseBurst(level, player, hand, pos);
        DustTriggerFx.emitTriggerSparkles(level, player, pos, trigger, start, placement);
        float pitch = SOUND_PITCH_BASE + (float) level.getRandom().nextGaussian() * SOUND_PITCH_DEVIATION;
        level.playSound(null, pos, TTSounds.DUST.get(), SoundSource.PLAYERS, SOUND_VOLUME, pitch);
    }

    private static void recordProgress(ServerPlayer player, RecipeHolder<DustTrigger> holder, ItemStack result, ItemStack consumed) {
        player.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
        ResearchProgressionEvents.recordCrafted(player, result);
        CriteriaTriggers.RECIPE_CRAFTED.trigger(player, holder.id(), List.of(consumed));
    }
}
