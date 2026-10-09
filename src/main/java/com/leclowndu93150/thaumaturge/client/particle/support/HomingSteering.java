package com.leclowndu93150.thaumaturge.client.particle.support;

import net.minecraft.util.Mth;

public final class HomingSteering {
    private final HomingTuning tuning;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public HomingSteering(HomingTuning tuning) {
        this.tuning = tuning;
    }

    public HomingPhase step(double x, double y, double z, double targetX, double targetY, double targetZ, double vx, double vy, double vz) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        if (distance < this.tuning.arrivalDistance()) {
            return HomingPhase.ARRIVED;
        }
        boolean near = distance < this.tuning.nearDistance();
        double scale = (near ? this.tuning.nearPush() : this.tuning.push()) / distance;
        double limit = this.tuning.maxSpeed();
        this.velocityX = Mth.clamp(vx + dx * scale, -limit, limit);
        this.velocityY = Mth.clamp(vy + dy * scale, -limit, limit);
        this.velocityZ = Mth.clamp(vz + dz * scale, -limit, limit);
        return near ? HomingPhase.NEAR : HomingPhase.FAR;
    }

    public double velocityX() {
        return this.velocityX;
    }

    public double velocityY() {
        return this.velocityY;
    }

    public double velocityZ() {
        return this.velocityZ;
    }
}
