package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class LeadingAim {
    private static final double CURRENT_MOTION = 1.0;
    private static final double MID_BODY = 0.5;

    private LeadingAim() {}

    public static Vec3 at(Entity shooter, Entity target) {
        return at(shooter, target, CURRENT_MOTION);
    }

    public static Vec3 at(Entity shooter, Entity target, double leadTicks) {
        Vec3 drift = target.getDeltaMovement().scale(leadTicks);
        return new Vec3(target.getX() + drift.x - shooter.getX(), target.getY(MID_BODY) - shooter.getY(), target.getZ() + drift.z - shooter.getZ());
    }
}
