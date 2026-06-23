package com.leclowndu93150.thaumcraft.content.item;

import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class ThaumometerItem extends Item {
    private static final float SCAN_VOLUME = 0.5F;
    private static final float SCAN_PITCH = 1.0F;

    public ThaumometerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    TCSounds.SCAN.get(), SoundSource.PLAYERS, SCAN_VOLUME, SCAN_PITCH);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendOverlayMessage(Component.translatable("item.thaumcraft.thaumometer.scan.todo"));
            }
        }
        player.swing(hand);
        return InteractionResult.SUCCESS;
    }
}
