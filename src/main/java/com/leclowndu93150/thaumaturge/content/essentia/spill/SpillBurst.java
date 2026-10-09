package com.leclowndu93150.thaumaturge.content.essentia.spill;

import net.minecraft.sounds.SoundEvents;

public record SpillBurst(int ventCount, SpillRange boxFraction, SpillRange riseSpeed, int color, float scale, boolean flame, float fluxPerUnit, SpillSound sound) {
    private static final int PIPE_VENTS = 5;
    private static final double PIPE_BOX_START = 0.33;
    private static final double PIPE_BOX_WIDTH = 0.33;
    private static final double PIPE_RISE_START = 0.1;
    private static final double PIPE_RISE_WIDTH = 0.2;
    private static final int PIPE_COLOR = 0x800080;
    private static final float PIPE_SCALE = 1.0F;
    private static final float PIPE_FLUX_PER_UNIT = 1.0F;
    private static final float PIPE_VOLUME = 0.1F;
    private static final float PIPE_PITCH_BASE = 1.0F;
    private static final float PIPE_PITCH_SPREAD = 0.1F;

    public static final SpillBurst PIPE_FLUX = new SpillBurst(PIPE_VENTS, SpillRange.startingAt(PIPE_BOX_START, PIPE_BOX_WIDTH), SpillRange.startingAt(PIPE_RISE_START, PIPE_RISE_WIDTH), PIPE_COLOR,
            PIPE_SCALE, true, PIPE_FLUX_PER_UNIT, new SpillSound(SoundEvents.LAVA_EXTINGUISH, PIPE_VOLUME, PIPE_PITCH_BASE, PIPE_PITCH_SPREAD));
}
