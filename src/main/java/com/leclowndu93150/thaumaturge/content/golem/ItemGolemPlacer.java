package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ItemGolemPlacer extends Item implements ISealDisplayer {
    private static final String RANK_KEY = "tooltip.thaumaturge.golem.rank";
    private static final String RANK_LABEL_KEY = "tooltip.thaumaturge.golem.rank_label";
    private static final String RANK_PROGRESS_KEY = "tooltip.thaumaturge.golem.rank_progress";
    private static final String XP_KEY = "tooltip.thaumaturge.golem.xp";
    private static final String TRAIT_KEY = "tooltip.thaumaturge.golem.trait";
    private static final float SPAWN_YAW = 0.0F;
    private static final float SPAWN_PITCH = 0.0F;
    private static final double CENTER = 0.5D;

    public ItemGolemPlacer(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        GolemProperties properties = stack.get(TTDataComponents.GOLEM_PROPERTIES.get());
        if (properties != null) {
            tooltipLines(stack, properties).forEach(builder);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!isSolidSurface(level, context.getClickedPos())) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos spot = context.getClickedPos().relative(context.getClickedFace());
        ItemStack held = context.getItemInHand();
        Player player = context.getPlayer();
        boolean allowed = player != null && player.mayUseItemAt(spot, context.getClickedFace(), held);
        if (!allowed || spawnGolem(serverLevel, spot, player, held) == null) {
            return InteractionResult.FAIL;
        }
        held.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean isSolidSurface(Level level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static EntityThaumaturgeGolem spawnGolem(ServerLevel level, BlockPos spot, Player owner, ItemStack source) {
        EntityThaumaturgeGolem golem = TTEntities.THAUMATURGE_GOLEM.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (golem == null) {
            return null;
        }
        golem.snapTo(spot.getX() + CENTER, spot.getY(), spot.getZ() + CENTER, SPAWN_YAW, SPAWN_PITCH);
        golem.setValidSpawn();
        golem.setOwner(owner);
        Optional.ofNullable(source.get(TTDataComponents.GOLEM_PROPERTIES.get())).ifPresent(golem::setProperties);
        golem.setRankXp(source.getOrDefault(TTDataComponents.GOLEM_XP.get(), 0));
        golem.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.MOB_SUMMONED, null);
        return level.addFreshEntity(golem) ? golem : null;
    }

    private static List<Component> tooltipLines(ItemStack stack, GolemProperties properties) {
        List<Component> lines = new ArrayList<>();
        if (properties.hasTrait(TTGolemTraits.SMART.get())) {
            lines.add(rankLine(stack, properties));
        }
        Optional.ofNullable(TTGolemParts.materials().getKey(properties.material())).map(id -> Component.translatable(GolemMaterial.nameKey(id)).withStyle(ChatFormatting.GREEN)).ifPresent(lines::add);
        properties.traits().stream().map(TTGolemTraits.registry()::getKey).filter(Objects::nonNull).map(ItemGolemPlacer::traitLine).forEach(lines::add);
        return lines;
    }

    private static Component traitLine(Identifier traitId) {
        return Component.translatable(TRAIT_KEY, Component.translatable(GolemTrait.nameKey(traitId))).withStyle(ChatFormatting.BLUE);
    }

    private static Component rankLine(ItemStack stack, GolemProperties properties) {
        int rank = properties.rank();
        MutableComponent rankText = Component.translatable(RANK_KEY, Component.translatable(RANK_LABEL_KEY), rank).withStyle(ChatFormatting.GOLD);
        if (rank < EntityThaumaturgeGolem.MAX_RANK) {
            int needed = EntityThaumaturgeGolem.xpForNextRank(rank);
            int stored = stack.getOrDefault(TTDataComponents.GOLEM_XP.get(), 0);
            return Component.translatable(RANK_PROGRESS_KEY, rankText, Component.translatable(XP_KEY, stored, needed).withStyle(ChatFormatting.DARK_GREEN));
        }
        return rankText;
    }
}
