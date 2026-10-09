package com.leclowndu93150.thaumaturge.content.essentia.cadence;

import net.minecraft.core.BlockPos;

public final class TickCadence {
    private static final int STAGGER_X = 31;
    private static final int STAGGER_Y = 17;
    private static final int STAGGER_Z = 13;
    private static final int STAGGER_SPAN = 10;

    private int ticks;

    private TickCadence(int startOffset) {
        this.ticks = startOffset;
    }

    public static TickCadence staggered(BlockPos pos) {
        return new TickCadence(Math.floorMod(STAGGER_X * pos.getX() + STAGGER_Y * pos.getY() + STAGGER_Z * pos.getZ(), STAGGER_SPAN));
    }

    public void advance() {
        ticks++;
    }

    public boolean isDue(CadencePhase phase) {
        return phase.isDue(ticks);
    }
}
