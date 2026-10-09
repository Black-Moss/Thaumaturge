package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class CreeperHissOutcome implements WarpOutcome {
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 0.5F;

    @Override
    public void apply(WarpRoll roll) {
        if (ThaumaturgeCommonConfig.NO_STRESS.get()) {
            return;
        }
        ServerPlayer player = roll.player();
        roll.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CREEPER_PRIMED, SoundSource.AMBIENT, VOLUME, PITCH);
    }
}
