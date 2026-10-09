package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import net.minecraft.world.phys.Vec3;

public final class StreamStep {
    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private double distance;

    public void measure(Vec3 target, HeadMotion head) {
        this.offsetX = target.x - head.x();
        this.offsetY = target.y - head.y();
        this.offsetZ = target.z - head.z();
        this.distance = Math.sqrt(this.offsetX * this.offsetX + this.offsetY * this.offsetY + this.offsetZ * this.offsetZ);
    }

    public double distance() {
        return this.distance;
    }

    public double directionX() {
        return this.offsetX / this.distance;
    }

    public double directionY() {
        return this.offsetY / this.distance;
    }

    public double directionZ() {
        return this.offsetZ / this.distance;
    }
}
