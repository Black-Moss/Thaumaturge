package com.leclowndu93150.thaumaturge.client.hud.tag;

import org.jspecify.annotations.Nullable;

public final class TagAnimation {
    private static final float GROWTH = 0.031F;
    private static final float DAMPING_DIVISOR = 10.0F;

    private final float cap;
    private @Nullable Object target;
    private float scale;

    public TagAnimation(float cap) {
        this.cap = cap;
    }

    public float advance(Object currentTarget) {
        if (!currentTarget.equals(target)) {
            target = currentTarget;
            scale = 0.0F;
        }
        if (scale < cap) {
            scale += GROWTH - scale / DAMPING_DIVISOR;
        }
        return scale;
    }

    public void reset() {
        target = null;
        scale = 0.0F;
    }
}
