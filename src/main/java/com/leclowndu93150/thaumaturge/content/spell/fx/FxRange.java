package com.leclowndu93150.thaumaturge.content.spell.fx;

import net.minecraft.util.RandomSource;

record FxRange(float base, float spread) {
    static final FxRange NONE = new FxRange(0.0F, 0.0F);

    float uniform(RandomSource random) {
        return base + spread * random.nextFloat();
    }

    float gaussian(RandomSource random) {
        return base + (float) random.nextGaussian() * spread;
    }

    int whole(RandomSource random) {
        int span = (int) spread;
        return span > 0 ? (int) base + random.nextInt(span) : (int) base;
    }
}
