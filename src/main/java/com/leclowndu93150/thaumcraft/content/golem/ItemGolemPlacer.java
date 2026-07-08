package com.leclowndu93150.thaumcraft.content.golem;

import com.leclowndu93150.thaumcraft.api.golems.GolemTrait;
import com.leclowndu93150.thaumcraft.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCGolemParts;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    public ItemGolemPlacer(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        GolemProperties props = stack.get(TCDataComponents.GOLEM_PROPERTIES.get());
        if (props == null) {
            return;
        }
        if (props.hasTrait(GolemTrait.SMART)) {
            if (props.getRank() >= EntityThaumcraftGolem.MAX_RANK) {
                tooltip.accept(Component.translatable("golem.rank")
                        .append(" " + props.getRank()).withStyle(ChatFormatting.GOLD));
            } else {
                int xp = stack.getOrDefault(TCDataComponents.GOLEM_XP.get(), 0);
                int needed = (props.getRank() + 1) * (props.getRank() + 1) * EntityThaumcraftGolem.XP_PER_RANK_UNIT;
                tooltip.accept(Component.translatable("golem.rank")
                        .append(" " + props.getRank()).withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(" (" + xp + "/" + needed + ")").withStyle(ChatFormatting.DARK_GREEN)));
            }
        }
        Identifier materialKey = TCGolemParts.materials().getKey(props.getMaterial());
        if (materialKey != null) {
            tooltip.accept(Component.translatable("golem.material." + materialKey.getPath())
                    .withStyle(ChatFormatting.GREEN));
        }
        for (GolemTrait trait : props.getTraits()) {
            tooltip.accept(Component.literal("-")
                    .append(Component.translatable("golem.trait." + trait.getSerializedName()))
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = clicked.relative(context.getClickedFace());
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        EntityThaumcraftGolem golem = TCEntities.THAUMCRAFT_GOLEM.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (golem == null) {
            return InteractionResult.FAIL;
        }
        golem.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        if (!serverLevel.addFreshEntity(golem)) {
            return InteractionResult.FAIL;
        }
        golem.setValidSpawn();
        golem.setOwner(player);
        ItemStack held = context.getItemInHand();
        GolemProperties props = held.get(TCDataComponents.GOLEM_PROPERTIES.get());
        if (props != null) {
            golem.setProperties(props.copy());
        }
        golem.setRankXp(held.getOrDefault(TCDataComponents.GOLEM_XP.get(), 0));
        golem.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
        if (!player.hasInfiniteMaterials()) {
            held.shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
