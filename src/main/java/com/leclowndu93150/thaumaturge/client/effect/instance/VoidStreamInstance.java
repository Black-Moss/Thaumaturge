package com.leclowndu93150.thaumaturge.client.effect.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VoidStreamInstance extends StreamInstance {
    private static final int WHITE = 0xFFFFFF;
    private static final int STREAM_LENGTH = 40;
    private static final int AGE_FACTOR = 2;
    private static final float LAUNCH_AMPLITUDE = 0.025F;
    private static final double MOTION_LIMIT = 0.04;
    private static final double PULL = 0.01;
    private static final double JITTER = 0.015;
    private static final double SHRINK_RANGE = 0.5;
    private static final float SNAPSHOT_WOBBLE = 0.01F;

    private final Vec3 target;

    public VoidStreamInstance(double x, double y, double z, double targetX, double targetY, double targetZ, int seed, float scale) {
        super(x, y, z, WHITE, seed, scale);
        this.target = new Vec3(targetX, targetY, targetZ);
        this.length = STREAM_LENGTH;
        this.maxAge = AGE_FACTOR * travelTicks(x, y, z, targetX, targetY, targetZ);
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
    protected void colour(float[] out, int slot) {
        out[0] = 1.0F;
        out[1] = 1.0F;
        out[2] = 1.0F;
        out[3] = 1.0F;
    }

    public @Nullable Snapshot snapshotWithRadiusMul(float partialTick, float radiusMultiplier) {
        return buildSnapshot(partialTick, SNAPSHOT_WOBBLE, false, radiusMultiplier);
    }
}
