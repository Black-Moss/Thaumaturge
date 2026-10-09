package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public record CompletionEffect(SoundEvent sound, float volume, float pitchCenter, float pitchReach) {
    private static final float DONE_VOLUME = 0.25F;
    private static final float DONE_PITCH_CENTER = 2.6F;
    private static final float DONE_PITCH_REACH = 0.8F;

    public static final CompletionEffect DEFAULT = new CompletionEffect(SoundEvents.LAVA_EXTINGUISH, DONE_VOLUME, DONE_PITCH_CENTER, DONE_PITCH_REACH);

    public float rollPitch(RandomSource random) {
        float first = random.nextFloat();
        float second = random.nextFloat();
        return pitchCenter + (first - second) * pitchReach;
    }

    public void play(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, rollPitch(level.getRandom()));
    }
}
