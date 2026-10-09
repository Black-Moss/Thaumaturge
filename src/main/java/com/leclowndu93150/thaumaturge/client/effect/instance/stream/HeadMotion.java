package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import net.minecraft.world.phys.Vec3;

public final class HeadMotion {
    private static final double RISE_PER_TICK = 0.002;
    private static final double DAMPING = 0.985;
    private static final int[] WAVE_DIVISORS = {4, 3, 2};

    private double x;
    private double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;

    public HeadMotion(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void launch(Vec3 heading, int waveSeed, double wobble, double verticalBoost) {
        this.motionX = heading.x + Math.sin(waveSeed / WAVE_DIVISORS[0]) * wobble;
        this.motionY = heading.y + verticalBoost + Math.sin(waveSeed / WAVE_DIVISORS[1]) * wobble;
        this.motionZ = heading.z + Math.sin(waveSeed / WAVE_DIVISORS[2]) * wobble;
    }

    public void advance() {
        this.motionY += RISE_PER_TICK;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
        this.motionX *= DAMPING;
        this.motionY *= DAMPING;
        this.motionZ *= DAMPING;
    }

    public void clamp(double limit) {
        this.motionX = Math.max(-limit, Math.min(limit, this.motionX));
        this.motionY = Math.max(-limit, Math.min(limit, this.motionY));
        this.motionZ = Math.max(-limit, Math.min(limit, this.motionZ));
    }

    public void accelerate(double ax, double ay, double az) {
        this.motionX += ax;
        this.motionY += ay;
        this.motionZ += az;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public Vec3 position() {
        return new Vec3(this.x, this.y, this.z);
    }
}
