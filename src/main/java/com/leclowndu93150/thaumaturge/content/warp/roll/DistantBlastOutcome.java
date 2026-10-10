package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class DistantBlastOutcome implements WarpOutcome {
    private static final double REACH = 10.0;
    private static final float CARRYING_VOLUME = 4.0F;
    private static final float LOW_PITCH = 0.6F;
    private static final float PITCH_WOBBLE = 0.1F;

    @Override
    public void apply(WarpRoll roll) {
        if (ThaumaturgeCommonConfig.NO_STRESS.get()) {
            return;
        }
        ServerPlayer player = roll.player();
        RandomSource random = roll.random();
        double x = player.getX() + nearBiased(random);
        double y = player.getY() + nearBiased(random);
        double z = player.getZ() + nearBiased(random);
        float pitch = LOW_PITCH + (random.nextFloat() - random.nextFloat()) * PITCH_WOBBLE;
        player.connection.send(new ClientboundSoundPacket(SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, x, y, z, CARRYING_VOLUME, pitch, random.nextLong()));
    }

    private static double nearBiased(RandomSource random) {
        return (random.nextDouble() - random.nextDouble()) * REACH;
    }
}
