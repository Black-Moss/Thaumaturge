package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class ItemCreativeFluxSponge extends Item {
    private static final int CHUNK_RADIUS = 4;
    private static final int CHUNK_SHIFT = 4;
    private static final int CHUNK_CENTER_OFFSET = 8;
    private static final float DRAIN_PER_CHUNK = 500.0F;
    private static final double RIFT_RANGE = 32.0;
    private static final float SOUND_VOLUME = 0.15F;
    private static final float SOUND_PITCH = 1.0F;

    private static final List<Component> TOOLTIP = List.of(Component.translatable("tooltip.thaumaturge.flux_sponge.drain.0").withStyle(ChatFormatting.GREEN),
            Component.translatable("tooltip.thaumaturge.flux_sponge.drain.1").withStyle(ChatFormatting.GREEN),
            Component.translatable("tooltip.thaumaturge.flux_sponge.rifts.0").withStyle(ChatFormatting.DARK_AQUA),
            Component.translatable("tooltip.thaumaturge.flux_sponge.rifts.1").withStyle(ChatFormatting.DARK_AQUA),
            Component.translatable("tooltip.thaumaturge.flux_sponge.creative").withStyle(ChatFormatting.DARK_PURPLE));

    public ItemCreativeFluxSponge(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        TOOLTIP.forEach(builder);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer user)) {
            return InteractionResult.SUCCESS;
        }
        level.playSound(null, user.getX(), user.getY(), user.getZ(), TTSounds.CRAFTSTART.get(), SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        int drained = (int) drainArea(level, user);
        user.sendSystemMessage(Component.translatable("message.thaumaturge.flux_sponge.drained", drained).withStyle(ChatFormatting.GREEN));
        if (user.isShiftKeyDown()) {
            int erased = eraseRifts(level, user);
            user.sendSystemMessage(Component.translatable("message.thaumaturge.flux_sponge.rifts", erased).withStyle(ChatFormatting.DARK_AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    private static double drainArea(Level level, ServerPlayer user) {
        int centerChunkX = user.blockPosition().getX() >> CHUNK_SHIFT;
        int centerChunkZ = user.blockPosition().getZ() >> CHUNK_SHIFT;
        int y = user.getBlockY();
        double total = 0.0;
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                BlockPos middle = new BlockPos(((centerChunkX + dx) << CHUNK_SHIFT) + CHUNK_CENTER_OFFSET, y, ((centerChunkZ + dz) << CHUNK_SHIFT) + CHUNK_CENTER_OFFSET);
                total += AuraHelper.drainFlux(level, middle, DRAIN_PER_CHUNK, false);
            }
        }
        return total;
    }

    private static int eraseRifts(Level level, ServerPlayer user) {
        AABB area = user.getBoundingBox().inflate(RIFT_RANGE);
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class, area);
        rifts.forEach(EntityFluxRift::discard);
        return rifts.size();
    }
}
