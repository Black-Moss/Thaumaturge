package com.leclowndu93150.thaumaturge.content.essentia.spill;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SpillSound(SoundEvent event, float volume, float pitchBase, float pitchSpread) {
    public void play(Level level, Vec3 at, RandomSource random) {
        level.playSound(null, at.x, at.y, at.z, event, SoundSource.BLOCKS, volume, pitchBase + random.nextFloat() * pitchSpread);
    }
}
