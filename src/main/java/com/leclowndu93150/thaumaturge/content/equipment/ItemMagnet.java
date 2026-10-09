package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ItemMagnet {
    private static final double PULL_STRENGTH = 0.3;
    private static final double LIFT = 0.1;
    private static final double MAX_SPEED = 0.25;

    private ItemMagnet() {}

    public static Vec3 pull(Vec3 velocity, Vec3 position, Vec3 target) {
        Vec3 offset = position.subtract(target);
        double distance = offset.length();
        if (distance == 0.0) {
            return velocity;
        }
        Vec3 direction = offset.scale(1.0 / distance);
        return clamp(velocity.subtract(direction.scale(PULL_STRENGTH)).add(0.0, LIFT, 0.0));
    }

    private static Vec3 clamp(Vec3 velocity) {
        return new Vec3(limit(velocity.x), limit(velocity.y), limit(velocity.z));
    }

    private static double limit(double speed) {
        return Mth.clamp(speed, -MAX_SPEED, MAX_SPEED);
    }
}
