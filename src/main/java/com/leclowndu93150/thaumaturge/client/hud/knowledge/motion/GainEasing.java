package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import net.minecraft.util.Mth;

public final class GainEasing {
    private static final float POP_SWELL = 0.8F;
    private static final float FLIGHT_SIZE_HOLD = 0.5F;
    private static final float DROP_SPAN = 0.8F;
    private static final float SWEEP_DELAY = 0.15F;

    private GainEasing() {}

    public static float popScale(float progress) {
        float p = Mth.clamp(progress, 0.0F, 1.0F);
        float remainder = 1.0F - p;
        float growth = 1.0F - remainder * remainder * remainder;
        return growth * (1.0F + POP_SWELL * Mth.sin(Mth.PI * p));
    }

    public static float flightScale(float remaining) {
        float q = Mth.clamp(remaining, 0.0F, 1.0F);
        return q * (1.0F + FLIGHT_SIZE_HOLD * (1.0F - q));
    }

    public static float drop(float progress) {
        return smooth(progress / DROP_SPAN);
    }

    public static float sweep(float progress) {
        return smooth((progress - SWEEP_DELAY) / (1.0F - SWEEP_DELAY));
    }

    public static float bell(float progress) {
        float wave = Mth.sin(Mth.PI * Mth.clamp(progress, 0.0F, 1.0F));
        return wave * wave;
    }

    private static float smooth(float value) {
        float x = Mth.clamp(value, 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}
