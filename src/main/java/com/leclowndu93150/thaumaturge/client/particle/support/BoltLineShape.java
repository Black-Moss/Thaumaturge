package com.leclowndu93150.thaumaturge.client.particle.support;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class BoltLineShape {
    public static final int AXES = 3;

    private static final int MIN_SECTIONS = 2;
    private static final int MAX_SECTIONS = 512;
    private static final double MIN_LENGTH = 1.0E-6;
    private static final float JITTER_RANGE = 0.1F;
    private static final double[] SWAY_DIVISORS = {4.0, 3.0, 2.0};

    private final int sections;
    private final double[] base;
    private final float[] sway;
    private final float[] jitter;

    public BoltLineShape(double originX, double originY, double originZ, double targetX, double targetY, double targetZ, RandomSource random, double phase) {
        double length = Math.sqrt(Mth.square(targetX - originX) + Mth.square(targetY - originY) + Mth.square(targetZ - originZ));
        boolean usable = Double.isFinite(length) && length > MIN_LENGTH;
        this.sections = usable ? Mth.clamp((int) (length * Math.PI), MIN_SECTIONS, MAX_SECTIONS) : 0;
        int pointCount = usable ? this.sections + 1 : 0;
        this.base = new double[pointCount * AXES];
        this.sway = new float[pointCount * AXES];
        this.jitter = new float[pointCount * AXES];
        if (!usable) {
            return;
        }
        double step = length * Math.PI / this.sections;
        for (int i = 0; i <= this.sections; i++) {
            double t = (double) i / this.sections;
            int offset = i * AXES;
            this.base[offset] = Mth.lerp(t, originX, targetX);
            this.base[offset + 1] = Mth.lerp(t, originY, targetY);
            this.base[offset + 2] = Mth.lerp(t, originZ, targetZ);
            if (i == 0 || i == this.sections) {
                continue;
            }
            double along = i * step + phase;
            for (int axis = 0; axis < AXES; axis++) {
                this.sway[offset + axis] = (float) Math.sin(along / SWAY_DIVISORS[axis]);
                this.jitter[offset + axis] = (random.nextFloat() - random.nextFloat()) * JITTER_RANGE;
            }
        }
    }

    public boolean isEmpty() {
        return this.sections == 0;
    }

    public int sections() {
        return this.sections;
    }

    public double coordinate(int index, int axis, double swayAmplitude) {
        int offset = index * AXES + axis;
        return this.base[offset] + swayAmplitude * this.sway[offset] + this.jitter[offset];
    }
}
