package com.leclowndu93150.thaumaturge.client.effect.instance.beam;

import net.minecraft.world.phys.Vec3;

public record BeamPose(Vec3 source, float yaw, float pitch) {
    public static BeamPose between(Vec3 source, Vec3 target) {
        Vec3 delta = source.subtract(target);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(delta.x, delta.z));
        float pitch = (float) Math.toDegrees(Math.atan2(delta.y, horizontal));
        return new BeamPose(source, yaw, pitch);
    }
}
