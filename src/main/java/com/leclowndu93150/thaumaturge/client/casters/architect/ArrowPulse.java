package com.leclowndu93150.thaumaturge.client.casters.architect;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

final class ArrowPulse {
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final float TICKS_PER_RADIAN = TICKS_PER_SECOND / Mth.TWO_PI;
    private static final int PHASE_X = 3;
    private static final int PHASE_Y = 5;
    private static final int PHASE_Z = 7;
    private static final int PHASE_WRAP = 44;

    private static final float RED_SWING = 0.16F;
    private static final float RED_MID = 0.30F;
    private static final float GREEN_SWING = 0.20F;
    private static final float GREEN_MID = 0.62F;
    private static final float BLUE_SWING = 0.06F;
    private static final float BLUE_MID = 0.96F;

    private static final PulseChannel RED = new PulseChannel(TICKS_PER_RADIAN, RED_SWING, RED_MID);
    private static final PulseChannel GREEN = new PulseChannel(TICKS_PER_RADIAN, GREEN_SWING, GREEN_MID);
    private static final PulseChannel BLUE = new PulseChannel(TICKS_PER_RADIAN, BLUE_SWING, BLUE_MID);

    private ArrowPulse() {}

    static float red(int ticks, BlockPos anchor) {
        return RED.sample(ticks, phase(anchor));
    }

    static float green(int ticks, BlockPos anchor) {
        return GREEN.sample(ticks, phase(anchor));
    }

    static float blue(int ticks, BlockPos anchor) {
        return BLUE.sample(ticks, phase(anchor));
    }

    private static int phase(BlockPos anchor) {
        return Math.floorMod(anchor.getX() * PHASE_X + anchor.getY() * PHASE_Y + anchor.getZ() * PHASE_Z, PHASE_WRAP);
    }
}
