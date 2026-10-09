package com.leclowndu93150.thaumaturge.client.particle.support;

import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class ParticleCulling {
    private static final double RANGE_FANCY = 50.0;
    private static final double RANGE_FAST = 25.0;

    private ParticleCulling() {}

    public static boolean beyondSpawnRange(double x, double y, double z) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return false;
        }
        Vec3 position = camera.position();
        double range = minecraft.options.graphicsPreset().get() == GraphicsPreset.FAST ? RANGE_FAST : RANGE_FANCY;
        return position.distanceToSqr(x, y, z) > range * range;
    }
}
