package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class DistantBlastOutcome implements WarpOutcome {
    private static final int OFFSET_BOUND = 10;
    private static final float CARRYING_VOLUME = 4.0F;
    private static final float BASE_PITCH = 1.0F;
    private static final float PITCH_WOBBLE = 0.2F;
    private static final float PITCH_SCALE = 0.7F;

    @Override
    public void apply(WarpRoll roll) {
        if (ThaumaturgeCommonConfig.NO_STRESS.get()) {
            return;
        }
        ServerPlayer player = roll.player();
        RandomSource random = roll.random();
        double x = player.getX() + blockOffset(random);
        double y = player.getY() + blockOffset(random);
        double z = player.getZ() + blockOffset(random);
        float pitch = (BASE_PITCH + (random.nextFloat() - random.nextFloat()) * PITCH_WOBBLE) * PITCH_SCALE;
        roll.level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, CARRYING_VOLUME, pitch);
    }

    private static int blockOffset(RandomSource random) {
        return random.nextInt(OFFSET_BOUND) - random.nextInt(OFFSET_BOUND);
    }
}
