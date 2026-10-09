package com.leclowndu93150.thaumaturge.client.effect.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BoreStreamInstance extends StreamInstance {
    private static final int MIN_LENGTH = 5;
    private static final int AGE_PER_LENGTH = 10;
    private static final float LAUNCH_AMPLITUDE = 0.15F;
    private static final double SPEED_DIVISOR = 10.0;
    private static final float SNAPSHOT_WOBBLE = 0.03F;
    private static final float RADIUS_MULTIPLIER = 1.0F;

    private final int targetEntityId;

    public BoreStreamInstance(double x, double y, double z, int targetEntityId, int color, int count, float scale, int extend, double verticalBoost) {
        super(x, y, z, color, count, scale);
        this.targetEntityId = targetEntityId;
        this.length = Math.max(MIN_LENGTH, extend);
        this.maxAge = this.length * AGE_PER_LENGTH;
        launchMotion(LAUNCH_AMPLITUDE, verticalBoost);
    }

    @Override
    protected @Nullable Vec3 seekTarget(ClientLevel level) {
        Entity entity = level.getEntity(this.targetEntityId);
        return entity == null ? null : new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
    }

    @Override
    protected void restrainMotion(double distance) {
        clampMotion(distance / SPEED_DIVISOR);
    }

    @Override
    protected double pullStrength(double distance) {
        return distance / SPEED_DIVISOR;
    }

    public @Nullable Snapshot snapshot(float partialTick) {
        return buildSnapshot(partialTick, SNAPSHOT_WOBBLE, false, RADIUS_MULTIPLIER);
    }
}
