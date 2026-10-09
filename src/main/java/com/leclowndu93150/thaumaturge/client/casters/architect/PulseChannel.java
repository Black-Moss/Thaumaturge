package com.leclowndu93150.thaumaturge.client.casters.architect;

import net.minecraft.util.Mth;

final class PulseChannel {
    private static final float CHANNEL_MAX = 1.0F;

    private final float period;
    private final float amplitude;
    private final float base;

    PulseChannel(float period, float amplitude, float base) {
        this.period = period;
        this.amplitude = amplitude;
        this.base = base;
    }

    float sample(int ticks, int phase) {
        return Math.min(Mth.sin(ticks / period + phase) * amplitude + base, CHANNEL_MAX);
    }
}
