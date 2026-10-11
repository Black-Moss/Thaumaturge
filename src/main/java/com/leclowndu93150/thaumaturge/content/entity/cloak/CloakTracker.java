package com.leclowndu93150.thaumaturge.content.entity.cloak;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class CloakTracker {
    private static final double SNAP_DISTANCE = 10.0;
    private static final double FOLLOW_RATE = 0.25;
    private static final float WALK_SCALE = 0.6F;
    private static final float MAX_BOB = 0.1F;
    private static final float BOB_RATE = 0.4F;

    private final Axis x = new Axis();
    private final Axis y = new Axis();
    private final Axis z = new Axis();
    private double lastX;
    private double lastZ;
    private float walkDistance;
    private float walkDistanceO;
    private float bob;
    private float bobO;
    private boolean primed;

    public void tick(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            return;
        }
        if (!primed) {
            prime(entity);
        }
        float moved = (float) Mth.length(entity.getX() - lastX, entity.getZ() - lastZ);
        lastX = entity.getX();
        lastZ = entity.getZ();
        walkDistanceO = walkDistance;
        if (moved < SNAP_DISTANCE) {
            walkDistance += moved * WALK_SCALE;
        }
        bobO = bob;
        bob = entity.isPassenger() ? 0.0F : bob + (targetBob(entity, moved) - bob) * BOB_RATE;
        x.follow(entity.getX());
        y.follow(entity.getY());
        z.follow(entity.getZ());
    }

    public double x(float partialTick) {
        return x.at(partialTick);
    }

    public double y(float partialTick) {
        return y.at(partialTick);
    }

    public double z(float partialTick) {
        return z.at(partialTick);
    }

    public float walkDistance(float partialTick) {
        return Mth.lerp(partialTick, walkDistanceO, walkDistance);
    }

    public float bob(float partialTick) {
        return Mth.lerp(partialTick, bobO, bob);
    }

    private void prime(LivingEntity entity) {
        x.snap(entity.getX());
        y.snap(entity.getY());
        z.snap(entity.getZ());
        lastX = entity.getX();
        lastZ = entity.getZ();
        primed = true;
    }

    private static float targetBob(LivingEntity entity, float moved) {
        return entity.onGround() && !entity.isDeadOrDying() && !entity.isSwimming() ? Math.min(MAX_BOB, moved) : 0.0F;
    }

    private static final class Axis {
        private double value;
        private double old;

        void follow(double target) {
            old = value;
            double gap = target - value;
            if (Math.abs(gap) > SNAP_DISTANCE) {
                snap(target);
            } else {
                value += gap * FOLLOW_RATE;
            }
        }

        void snap(double target) {
            value = target;
            old = target;
        }

        double at(float partialTick) {
            return Mth.lerp(partialTick, old, value);
        }
    }
}
