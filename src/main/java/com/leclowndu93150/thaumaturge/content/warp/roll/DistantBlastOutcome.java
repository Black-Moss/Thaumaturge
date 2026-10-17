package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.warp.spawn.SpawnSpots;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class DistantBlastOutcome implements WarpOutcome {
    private static final int OFFSET_BOUND = 10;
    private static final float VOLUME = 4.0F;
    private static final float PITCH_JITTER = 0.2F;
    private static final float PITCH_SCALE = 0.7F;

    @Override
    public void apply(WarpRoll roll) {
        if (ThaumaturgeCommonConfig.NO_STRESS.get()) {
            return;
        }
        ServerPlayer player = roll.player();
        RandomSource random = roll.random();
        double x = player.getX() + SpawnSpots.symmetricSpread(random, OFFSET_BOUND);
        double y = player.getY() + SpawnSpots.symmetricSpread(random, OFFSET_BOUND);
        double z = player.getZ() + SpawnSpots.symmetricSpread(random, OFFSET_BOUND);
        float pitch = (1.0F + (random.nextFloat() - random.nextFloat()) * PITCH_JITTER) * PITCH_SCALE;
        roll.level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, VOLUME, pitch);
    }
}
