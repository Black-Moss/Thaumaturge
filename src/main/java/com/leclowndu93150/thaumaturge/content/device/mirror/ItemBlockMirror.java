package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class ItemBlockMirror extends BlockItem {
    private static final String ALREADY_LINKED_KEY = "message.thaumaturge.mirror.already_linked";
    private static final String LINKED_TO_KEY = "tooltip.thaumaturge.mirror.linked_to";
    private static final float LINK_VOLUME = 1.0F;
    private static final float LINK_PITCH = 2.0F;
    private static final int LINKED_COPY_COUNT = 1;

    public ItemBlockMirror(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || !level.getBlockState(pos).is(getBlock())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof BlockEntityMirrorBase mirror)) {
            return InteractionResult.SUCCESS;
        }
        if (mirror.verifyPairing()) {
            player.sendSystemMessage(Component.translatable(ALREADY_LINKED_KEY).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            return InteractionResult.SUCCESS;
        }
        ItemStack linked = stack.copyWithCount(LINKED_COPY_COUNT);
        linked.set(TTDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(LINKED_COPY_COUNT);
        }
        give(player, linked);
        level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, LINK_VOLUME, LINK_PITCH);
        return InteractionResult.SUCCESS;
    }

    private static void give(Player player, ItemStack stack) {
        if (player.getInventory().add(stack)) {
            return;
        }
        Level level = player.level();
        ItemEntity dropped = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        dropped.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(dropped);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        GlobalPos link = stack.get(TTDataComponents.MIRROR_LINK.get());
        if (link != null) {
            BlockPos pos = link.pos();
            builder.accept(Component.translatable(LINKED_TO_KEY, pos.getX(), pos.getY(), pos.getZ(), link.dimension().identifier().toString()).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
