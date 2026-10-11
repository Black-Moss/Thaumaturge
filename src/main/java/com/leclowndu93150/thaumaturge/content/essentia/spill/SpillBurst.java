package com.leclowndu93150.thaumaturge.content.essentia.spill;

import net.minecraft.sounds.SoundEvents;

public record SpillBurst(int ventCount, SpillRange boxFraction, SpillRange riseSpeed, int color, float scale, boolean flame, float fluxPerUnit, SpillSound sound) {
    private static final int PIPE_VENTS = 4;
    private static final double PIPE_BOX_START = 0.3;
    private static final double PIPE_BOX_WIDTH = 0.4;
    private static final double PIPE_RISE_START = 0.004;
    private static final double PIPE_RISE_WIDTH = 0.012;
    private static final int PIPE_COLOR = 0x4E1C6E;
    private static final float PIPE_SCALE = 1.6F;
    private static final float PIPE_VOLUME = 0.25F;
    private static final float PIPE_PITCH_BASE = 1.7F;
    private static final float PIPE_PITCH_SPREAD = 0.6F;
    private static final float PIPE_FLUX_PER_UNIT = 1.0F;

    public static final SpillBurst PIPE_FLUX = new SpillBurst(PIPE_VENTS, SpillRange.startingAt(PIPE_BOX_START, PIPE_BOX_WIDTH), SpillRange.startingAt(PIPE_RISE_START, PIPE_RISE_WIDTH), PIPE_COLOR,
            PIPE_SCALE, true, PIPE_FLUX_PER_UNIT, new SpillSound(SoundEvents.LAVA_EXTINGUISH, PIPE_VOLUME, PIPE_PITCH_BASE, PIPE_PITCH_SPREAD));
}
