package com.leclowndu93150.thaumaturge.client.hud.hover;

import net.minecraft.util.Mth;

public final class FuelFraction {
    private FuelFraction() {}

    public static float of(int fuel, int maxFuel) {
        if (maxFuel <= 0) {
            return 0.0F;
        }
        return Mth.clamp(fuel / (float) maxFuel, 0.0F, 1.0F);
    }

    public static int pixels(float fraction, int fullHeight) {
        return Mth.clamp(Math.round(fraction * fullHeight), 0, fullHeight);
    }
}
