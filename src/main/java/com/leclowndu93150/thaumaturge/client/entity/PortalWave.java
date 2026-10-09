package com.leclowndu93150.thaumaturge.client.entity;

import net.minecraft.util.Mth;

final class PortalWave {
    private PortalWave() {}

    static float sineByDegrees(float steps, float degreesPerStep) {
        return Mth.sin(steps * degreesPerStep * Mth.DEG_TO_RAD);
    }

    static float period(float base, float rate, float amplitude) {
        return base - rate * amplitude;
    }

    static float offsetSine(float phase, float period, float amplitude) {
        return Mth.sin(phase / period) * amplitude + amplitude;
    }
}
