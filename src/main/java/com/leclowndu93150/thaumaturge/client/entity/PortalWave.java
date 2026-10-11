package com.leclowndu93150.thaumaturge.client.entity;

import net.minecraft.util.Mth;

public final class PortalWave {
    private static final float CALM_RATE = 0.035F;
    private static final float FRANTIC_RATE = 0.16F;
    private static final float WIDTH_RATE_RATIO = 1.37F;
    private static final float WIDTH_PHASE = 1.1F;
    private static final float MAX_HEIGHT_DIP = 0.16F;
    private static final float MAX_WIDTH_DIP = 0.22F;
    private static final float MAX_FADE = 0.62F;
    private static final float FLICKER_RATE = 0.9F;
    private static final float MAX_FLICKER = 0.08F;
    private static final float MIN_OPACITY = 0.2F;
    private static final float FLINCH_SQUASH = 0.14F;
    private static final float FLINCH_WIDEN = 0.1F;
    private static final float PULSE_SWELL = 0.28F;
    private static final float HALF = 0.5F;

    private PortalWave() {}

    public static float heightScale(float damage, float time, float flinch, float beat) {
        float breath = damage * MAX_HEIGHT_DIP * breath(damage, time, 1.0F, 0.0F);
        return (1.0F - breath) * (1.0F - FLINCH_SQUASH * flinch) * (1.0F + PULSE_SWELL * swell(beat));
    }

    public static float widthScale(float damage, float time, float flinch, float beat) {
        float breath = damage * MAX_WIDTH_DIP * breath(damage, time, WIDTH_RATE_RATIO, WIDTH_PHASE);
        return (1.0F - breath) * (1.0F + FLINCH_WIDEN * flinch) * (1.0F + PULSE_SWELL * swell(beat));
    }

    public static float opacity(float damage, float time) {
        float fade = damage * MAX_FADE * breath(damage, time, 1.0F, 0.0F);
        float flicker = damage * MAX_FLICKER * trough(time * FLICKER_RATE);
        return Mth.clamp(1.0F - fade - flicker, MIN_OPACITY, 1.0F);
    }

    private static float breath(float damage, float time, float rateRatio, float offset) {
        float calm = trough(time * CALM_RATE * rateRatio + offset);
        float frantic = trough(time * FRANTIC_RATE * rateRatio + offset);
        return Mth.lerp(damage, calm, frantic);
    }

    private static float trough(float angle) {
        return HALF + HALF * Mth.sin(angle);
    }

    private static float swell(float beat) {
        return beat <= 0.0F ? 0.0F : Mth.sin(beat * Mth.PI);
    }
}
