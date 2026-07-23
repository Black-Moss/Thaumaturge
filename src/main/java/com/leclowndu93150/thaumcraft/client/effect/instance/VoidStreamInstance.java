package com.leclowndu93150.thaumcraft.client.effect.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public final class VoidStreamInstance extends StreamInstance {
    private static final float LAUNCH_AMPLITUDE = 0.025F;
    private static final double MOTION_LIMIT = 0.04;
    private static final double PULL = 0.01;
    private static final double JITTER = 0.015;
    private static final double SHRINK_RANGE = 0.5;
    private static final int TRAIL_LENGTH = 40;
    private static final float WOBBLE = 0.01F;
    private static final int WHITE = 0xFFFFFF;

    private final Vec3 target;

    public VoidStreamInstance(double sx, double sy, double sz, double tx, double ty, double tz, int seed, float scale) {
        super(sx, sy, sz, WHITE, seed, scale);
        this.target = new Vec3(tx, ty, tz);
        this.length = TRAIL_LENGTH;
        this.maxAge = travelTicks(sx, sy, sz, tx, ty, tz) * 2;
        launchMotion(LAUNCH_AMPLITUDE, 0.0);
    }

    @Override
    protected Vec3 seekTarget(ClientLevel level) {
        return this.target;
    }

    @Override
    protected void restrainMotion(double distance) {
        clampMotion(MOTION_LIMIT);
    }

    @Override
    protected double pullStrength(double distance) {
        return PULL;
    }

    @Override
    protected double steerJitter(ClientLevel level) {
        return level.getRandom().nextGaussian() * JITTER;
    }

    @Override
    protected double shrinkRange() {
        return SHRINK_RANGE;
    }

    @Override
    protected void colour(float[] out, int beat) {
        out[0] = 1.0F;
        out[1] = 1.0F;
        out[2] = 1.0F;
        out[3] = 1.0F;
    }

    public Snapshot snapshotWithRadiusMul(float partialTick, float radiusMul) {
        return buildSnapshot(partialTick, WOBBLE, false, radiusMul);
    }
}
