package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import net.minecraft.util.Mth;

public final class GainEasing {
    private static final float PULSE_BASE = 1.5F;
    private static final float PULSE_AMPLITUDE = 0.5F;
    private static final float POP_PEAK_PROGRESS = 0.5F;
    private static final float POP_RAMP_FACTOR = 2.0F;
    private static final float DROP_BASE = 0.5F;

    private GainEasing() {}

    public static float popPulse(float progress) {
        return PULSE_BASE - PULSE_AMPLITUDE * Mth.cos(Mth.TWO_PI * progress);
    }

    public static float popScale(float progress) {
        float pulse = popPulse(progress);
        return progress >= POP_PEAK_PROGRESS ? pulse : POP_RAMP_FACTOR * progress * pulse;
    }

    public static float flightDrop(float flight) {
        return DROP_BASE - DROP_BASE * Mth.cos(Mth.PI * flight);
    }

    public static float flightSweep(float drop) {
        return Mth.sin(drop * Mth.HALF_PI);
    }
}
