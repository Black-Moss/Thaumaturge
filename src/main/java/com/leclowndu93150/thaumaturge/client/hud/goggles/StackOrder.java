package com.leclowndu93150.thaumaturge.client.hud.goggles;

import java.util.function.DoubleUnaryOperator;

public enum StackOrder {
    TOP_FIRST(offset -> -offset), BOTTOM_FIRST(offset -> offset);

    private final DoubleUnaryOperator mapping;

    StackOrder(DoubleUnaryOperator mapping) {
        this.mapping = mapping;
    }

    public double apply(double centredOffset) {
        return mapping.applyAsDouble(centredOffset);
    }
}
