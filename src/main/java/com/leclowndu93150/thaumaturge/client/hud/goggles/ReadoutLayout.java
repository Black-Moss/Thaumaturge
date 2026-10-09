package com.leclowndu93150.thaumaturge.client.hud.goggles;

public record ReadoutLayout(double lineStep, double blockCentre, float textScale, float baseline, float halfWidthFactor, int textColor) {
    private static final double LINE_STEP_BLOCKS = 1.0 / 5.5;
    private static final double BLOCK_CENTRE = 0.5;
    private static final float TEXT_SCALE = 0.0125F;
    private static final float BASELINE_PIXELS = 1.0F;
    private static final float HALF_WIDTH_FACTOR = 0.5F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    public static final ReadoutLayout STANDARD = new ReadoutLayout(LINE_STEP_BLOCKS, BLOCK_CENTRE, TEXT_SCALE, BASELINE_PIXELS, HALF_WIDTH_FACTOR, TEXT_COLOR);
}
