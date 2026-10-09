package com.leclowndu93150.thaumaturge.content.spell.fx;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;

@FunctionalInterface
interface FxFamily {
    ParticleOptions build(FxParams params, int color, RandomSource random);
}
