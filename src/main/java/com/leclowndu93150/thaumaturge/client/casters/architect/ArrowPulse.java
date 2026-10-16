package com.leclowndu93150.thaumaturge.client.casters.architect;

import net.minecraft.core.BlockPos;

final class ArrowPulse {
    private static final float RED_DIVISOR = 4.0F;
    private static final float GREEN_DIVISOR = 3.0F;
    private static final float BLUE_DIVISOR = 2.0F;

    private static final float RED_SWING = 0.2F;
    private static final float RED_MID = 0.3F;
    private static final float GREEN_SWING = 0.2F;
    private static final float GREEN_MID = 0.3F;
    private static final float BLUE_SWING = 0.2F;
    private static final float BLUE_MID = 0.8F;

    private static final PulseChannel RED = new PulseChannel(RED_DIVISOR, RED_SWING, RED_MID);
    private static final PulseChannel GREEN = new PulseChannel(GREEN_DIVISOR, GREEN_SWING, GREEN_MID);
    private static final PulseChannel BLUE = new PulseChannel(BLUE_DIVISOR, BLUE_SWING, BLUE_MID);

    private ArrowPulse() {}

    static float red(int ticks, BlockPos anchor) {
        return RED.sample(ticks, anchor.getX());
    }

    static float green(int ticks, BlockPos anchor) {
        return GREEN.sample(ticks, anchor.getY());
    }

    static float blue(int ticks, BlockPos anchor) {
        return BLUE.sample(ticks, anchor.getZ());
    }
}
