package com.leclowndu93150.thaumaturge.client.effect;

import net.minecraft.util.RandomSource;

public record ColorRange(int min, int max) {
    public static final float CHANNEL_MAX = 255.0F;

    public float roll(RandomSource random) {
        return (min + random.nextInt(max - min + 1)) / CHANNEL_MAX;
    }
}
