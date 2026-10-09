package com.leclowndu93150.thaumaturge.client.particle.support;

import net.minecraft.util.Mth;

public final class BoltLineShape {
    public static final int AXES = 3;

    private static final int MIN_SECTIONS = 2;
    private static final int MAX_SECTIONS = 512;
    private static final double MIN_LENGTH = 1.0E-6;
    private static final float JITTER_RANGE = 0.1F;
    private static final double TWO_PI = Math.PI * 2.0;
    private static final double[] WAVELENGTHS = {7.0, 11.0, 17.0};
    private static final double[] AXIS_PHASE = {0.0, Math.PI / 3.0, Math.PI * 2.0 / 3.0};
    private static final int HASH_STEP = 0x9E3779B1;
    private static final int HASH_FOLD_A = 16;
    private static final int HASH_FOLD_B = 13;
    private static final int HASH_MUL_A = 0x85EBCA6B;
    private static final int HASH_MUL_B = 0xC2B2AE35;
    private static final int HASH_UNIT_SHIFT = 8;
    private static final float HASH_UNIT_SCALE = 1.0F / (1 << 24);

    private final int seed;
    private final int sections;
    private final double[] base;
    private final float[] sway;
    private final float[] jitter;

    public BoltLineShape(double originX, double originY, double originZ, double targetX, double targetY, double targetZ, int seed, double phase) {
        this.seed = seed;
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
        for (int i = 0; i <= this.sections; i++) {
            double t = (double) i / this.sections;
            int offset = i * AXES;
            this.base[offset] = Mth.lerp(t, originX, targetX);
            this.base[offset + 1] = Mth.lerp(t, originY, targetY);
            this.base[offset + 2] = Mth.lerp(t, originZ, targetZ);
            if (i == 0 || i == this.sections) {
                continue;
            }
            for (int axis = 0; axis < AXES; axis++) {
                this.sway[offset + axis] = (float) Math.sin(phase + AXIS_PHASE[axis] + TWO_PI * i / WAVELENGTHS[axis]);
            }
        }
        refreshJitter(0);
    }

    public boolean isEmpty() {
        return this.sections == 0;
    }

    public int sections() {
        return this.sections;
    }

    public void refreshJitter(int tick) {
        for (int i = 1; i < this.sections; i++) {
            int offset = i * AXES;
            for (int axis = 0; axis < AXES; axis++) {
                this.jitter[offset + axis] = signedUnit(hash(i, tick, axis)) * JITTER_RANGE;
            }
        }
    }

    public double coordinate(int index, int axis, double swayAmplitude) {
        int offset = index * AXES + axis;
        return this.base[offset] + swayAmplitude * this.sway[offset] + this.jitter[offset];
    }

    private int hash(int index, int tick, int axis) {
        int h = this.seed;
        h = h * HASH_STEP + index;
        h = h * HASH_STEP + tick;
        h = h * HASH_STEP + axis;
        h ^= h >>> HASH_FOLD_A;
        h *= HASH_MUL_A;
        h ^= h >>> HASH_FOLD_B;
        h *= HASH_MUL_B;
        h ^= h >>> HASH_FOLD_A;
        return h;
    }

    private static float signedUnit(int hash) {
        return (hash >>> HASH_UNIT_SHIFT) * HASH_UNIT_SCALE * 2.0F - 1.0F;
    }
}
