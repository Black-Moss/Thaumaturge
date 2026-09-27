package com.leclowndu93150.thaumaturge.client.render;

import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.util.Mth;

public final class FogPlanes {
    private static final float MAX_SANE_FOG_PLANE = 4096.0F;
    private static final float FALLBACK_FAR_PLANE = 512.0F;
    private static final float FALLBACK_NEAR_PLANE = 256.0F;

    private FogPlanes() {}

    public static void pullToward(FogData fog, float intensity, float near, float far) {
        fog.environmentalEnd = Mth.lerp(intensity, usable(fog.environmentalEnd, FALLBACK_FAR_PLANE), far);
        fog.environmentalStart = Mth.lerp(intensity, usable(fog.environmentalStart, FALLBACK_NEAR_PLANE), near);
    }

    private static float usable(float plane, float fallback) {
        return plane > MAX_SANE_FOG_PLANE ? fallback : plane;
    }
}
