package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import net.minecraft.util.ARGB;

public record GainRolls(int tiltDegrees, int startX, int startY, int endX, int endY, GlintRoll appear, GlintRoll arrival) {
    private static final int TILT_BASE = 6;
    private static final int GREEN_BASE = 189;
    private static final int BLUE_BASE = 64;
    private static final int FULL_CHANNEL = 255;

    public static GainRolls of(long seed) {
        GlintRoll appear = glint(seed, RollSalt.APPEAR_FRAME, RollSalt.APPEAR_SPIN, RollSalt.APPEAR_GREEN, RollSalt.APPEAR_BLUE);
        GlintRoll arrival = glint(seed, RollSalt.ARRIVAL_FRAME, RollSalt.ARRIVAL_SPIN, RollSalt.ARRIVAL_GREEN, RollSalt.ARRIVAL_BLUE);
        return new GainRolls(TILT_BASE - RollSalt.TILT.roll(seed), RollSalt.START_X.roll(seed), RollSalt.START_Y.roll(seed), RollSalt.END_X.roll(seed), RollSalt.END_Y.roll(seed), appear, arrival);
    }

    private static GlintRoll glint(long seed, RollSalt frame, RollSalt spin, RollSalt green, RollSalt blue) {
        int color = ARGB.color(FULL_CHANNEL, FULL_CHANNEL, GREEN_BASE + green.roll(seed), BLUE_BASE + blue.roll(seed));
        return new GlintRoll(frame.roll(seed), spin.roll(seed), color);
    }
}
