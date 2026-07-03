package com.leclowndu93150.thaumcraft.content.fx;

import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

public final class WispFx {
    private static final int BURST_MAX_AGE = 31;
    private static final int BURST_GRID = 16;
    private static final int BURST_FRAME_START = 208;
    private static final int BURST_FRAME_COUNT = 31;
    private static final int MOTE_GRID = 64;
    private static final int MOTE_FRAME_START = 264;
    private static final int MOTE_FRAME_COUNT = 8;
    private static final int MOTE_BASE_AGE = 10;
    private static final int MOTE_AGE_SPREAD = 5;
    private static final float MOTE_ALPHA = 0.5F;
    private static final float MOTE_SCALE_SPREAD = 0.25F;
    private static final float MOTE_END_SCALE = 0.05F;
    private static final double MOTE_WIND = 2.5E-4;
    private static final float MOTE_JITTER = 0.0025F;

    private WispFx() {}

    public static FXGenericData burst(float size) {
        return FXGenericData.builder()
                .maxAge(BURST_MAX_AGE)
                .grid(BURST_GRID)
                .particles(BURST_FRAME_START, BURST_FRAME_COUNT, 1)
                .scale(size)
                .build();
    }

    public static FXGenericData mote(RandomSource random, int color) {
        return FXGenericData.builder()
                .maxAge(MOTE_BASE_AGE + random.nextInt(MOTE_AGE_SPREAD))
                .color(ARGB.red(color) / 255.0F, ARGB.green(color) / 255.0F, ARGB.blue(color) / 255.0F)
                .alpha(MOTE_ALPHA)
                .loop(true)
                .grid(MOTE_GRID)
                .particles(MOTE_FRAME_START, MOTE_FRAME_COUNT, 1)
                .scale(1.0F + random.nextFloat() * MOTE_SCALE_SPREAD, MOTE_END_SCALE)
                .wind(MOTE_WIND)
                .random(MOTE_JITTER, 0.0F, MOTE_JITTER)
                .build();
    }
}
