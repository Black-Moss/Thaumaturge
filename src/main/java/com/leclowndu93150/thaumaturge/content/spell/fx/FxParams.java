package com.leclowndu93150.thaumaturge.content.spell.fx;

record FxParams(FxRange scale, FxRange endScale, FxRange shade, FxRange age, FxRange delay, FxRange variant, float alpha, float decay, float vertical, int fixedColor, boolean toggle) {
    private static final float FULL_ALPHA = 1.0F;
    private static final float FULL_DECAY = 1.0F;

    static FxParams defaults() {
        return new FxParams(FxRange.NONE, FxRange.NONE, FxRange.NONE, FxRange.NONE, FxRange.NONE, FxRange.NONE, FULL_ALPHA, FULL_DECAY, 0.0F, 0, false);
    }

    FxParams scale(float base, float spread) {
        return new FxParams(new FxRange(base, spread), endScale, shade, age, delay, variant, alpha, decay, vertical, fixedColor, toggle);
    }

    FxParams endScale(float base, float spread) {
        return new FxParams(scale, new FxRange(base, spread), shade, age, delay, variant, alpha, decay, vertical, fixedColor, toggle);
    }

    FxParams shade(float base, float spread) {
        return new FxParams(scale, endScale, new FxRange(base, spread), age, delay, variant, alpha, decay, vertical, fixedColor, toggle);
    }

    FxParams age(int base, int spread) {
        return new FxParams(scale, endScale, shade, new FxRange(base, spread), delay, variant, alpha, decay, vertical, fixedColor, toggle);
    }

    FxParams delay(int base, int spread) {
        return new FxParams(scale, endScale, shade, age, new FxRange(base, spread), variant, alpha, decay, vertical, fixedColor, toggle);
    }

    FxParams variant(int base, int spread) {
        return new FxParams(scale, endScale, shade, age, delay, new FxRange(base, spread), alpha, decay, vertical, fixedColor, toggle);
    }

    FxParams alpha(float value) {
        return new FxParams(scale, endScale, shade, age, delay, variant, value, decay, vertical, fixedColor, toggle);
    }

    FxParams decay(float value) {
        return new FxParams(scale, endScale, shade, age, delay, variant, alpha, value, vertical, fixedColor, toggle);
    }

    FxParams vertical(float value) {
        return new FxParams(scale, endScale, shade, age, delay, variant, alpha, decay, value, fixedColor, toggle);
    }

    FxParams fixedColor(int value) {
        return new FxParams(scale, endScale, shade, age, delay, variant, alpha, decay, vertical, value, toggle);
    }

    FxParams toggle(boolean value) {
        return new FxParams(scale, endScale, shade, age, delay, variant, alpha, decay, vertical, fixedColor, value);
    }
}
