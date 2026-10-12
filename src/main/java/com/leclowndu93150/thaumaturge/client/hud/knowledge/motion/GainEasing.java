package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import net.minecraft.util.Mth;

public final class GainEasing {
    private static final float PULSE_BASE = 1.5F;
    private static final float PULSE_AMPLITUDE = 0.5F;
    private static final float POP_PEAK_POINT = 0.5F;
    private static final float ARC_BASE = 0.5F;
    private static final float ARC_AMPLITUDE = 0.5F;

    private GainEasing() {}

    public static float popScale(float progress) {
        float p = Mth.clamp(progress, 0.0F, 1.0F);
        float pulse = PULSE_BASE - PULSE_AMPLITUDE * Mth.cos(Mth.TWO_PI * p);
        if (p < POP_PEAK_POINT) {
            return 2.0F * p * pulse;
        }
        return pulse;
    }

    public static float flightScale(float remaining) {
        return Mth.clamp(remaining, 0.0F, 1.0F);
    }

    public static float verticalShare(float remaining) {
        float f = Mth.clamp(remaining, 0.0F, 1.0F);
        return ARC_BASE - ARC_AMPLITUDE * Mth.cos(Mth.PI * f);
    }

    public static float horizontalShare(float remaining) {
        return Mth.sin(verticalShare(remaining) * Mth.HALF_PI);
    }

    public static float bell(float progress) {
        float wave = Mth.sin(Mth.PI * Mth.clamp(progress, 0.0F, 1.0F));
        return wave * wave;
    }
}
