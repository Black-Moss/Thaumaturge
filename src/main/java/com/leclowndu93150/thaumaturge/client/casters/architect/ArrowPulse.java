package com.leclowndu93150.thaumaturge.client.casters.architect;

import net.minecraft.core.BlockPos;

final class ArrowPulse {
    private static final PulseChannel RED = new PulseChannel(4.0F, 0.2F, 0.3F);
    private static final PulseChannel GREEN = new PulseChannel(3.0F, 0.2F, 0.3F);
    private static final PulseChannel BLUE = new PulseChannel(2.0F, 0.2F, 0.8F);

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
