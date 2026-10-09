package com.leclowndu93150.thaumaturge.content.effect;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

record EffectColor(float r, float g, float b) {
    static final EffectColor WHITE = new EffectColor(1.0F, 1.0F, 1.0F);

    private static final float CHANNEL_MAX = 255.0F;
    private static final int CHANNEL_MAX_INT = 255;
    private static final int CHANNEL_MASK = 0xFF;
    private static final int OPAQUE_ALPHA = 0xFF000000;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final float RANDOM_MOTE_FLOOR = 0.25F;
    private static final float RANDOM_MOTE_SPAN = 0.75F;

    static EffectColor ofRgb(int rgb) {
        return new EffectColor(((rgb >> RED_SHIFT) & CHANNEL_MASK) / CHANNEL_MAX, ((rgb >> GREEN_SHIFT) & CHANNEL_MASK) / CHANNEL_MAX, (rgb & CHANNEL_MASK) / CHANNEL_MAX);
    }

    static EffectColor randomMote(RandomSource random) {
        return new EffectColor(RANDOM_MOTE_FLOOR + random.nextFloat() * RANDOM_MOTE_SPAN, RANDOM_MOTE_FLOOR + random.nextFloat() * RANDOM_MOTE_SPAN,
                RANDOM_MOTE_FLOOR + random.nextFloat() * RANDOM_MOTE_SPAN);
    }

    static int opaque(int rgb) {
        return OPAQUE_ALPHA | rgb;
    }

    static float mean(int rgb) {
        EffectColor color = ofRgb(rgb);
        return (color.r + color.g + color.b) / 3.0F;
    }

    int argb() {
        return OPAQUE_ALPHA | (channel(r) << RED_SHIFT) | (channel(g) << GREEN_SHIFT) | channel(b);
    }

    private static int channel(float value) {
        return Mth.clamp((int) (value * CHANNEL_MAX), 0, CHANNEL_MAX_INT);
    }
}
