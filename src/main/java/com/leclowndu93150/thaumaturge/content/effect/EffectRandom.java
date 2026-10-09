package com.leclowndu93150.thaumaturge.content.effect;

import net.minecraft.util.RandomSource;

final class EffectRandom {
    private EffectRandom() {}

    static double signedSpeed(RandomSource random, double base, double spread) {
        double speed = base + random.nextDouble() * spread;
        return random.nextBoolean() ? -speed : speed;
    }
}
