package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

public record RangeShrink(double range) implements ShrinkRule {
    private static final double QUARTER_TURN = Math.PI / 2.0;

    @Override
    public boolean applies(double distance) {
        return distance < this.range;
    }

    @Override
    public double factor(double distance) {
        return Math.sin(distance * QUARTER_TURN);
    }
}
