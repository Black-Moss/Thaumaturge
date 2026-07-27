package com.leclowndu93150.thaumaturge.client.warp;

public final class WarpFogState {
    public static final int MIST_DURATION_TICKS = 2400;
    public static final int MIST_SHORT_DURATION_TICKS = 200;

    private static int fogTicks;

    private WarpFogState() {}

    public static void startMist() {
        fogTicks = MIST_DURATION_TICKS;
    }

    public static void startShortMist() {
        fogTicks = Math.max(fogTicks, MIST_SHORT_DURATION_TICKS);
    }

    public static void tick() {
        if (fogTicks > 0) {
            fogTicks--;
        }
    }

    public static boolean active() {
        return fogTicks > 0;
    }

    public static float intensity() {
        if (fogTicks <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, fogTicks / 100.0F);
    }

    public static void reset() {
        fogTicks = 0;
    }
}
