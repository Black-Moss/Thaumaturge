package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HoverPhysics implements WhirlwindStrategy {
    private static final double LIFT = 0.08;
    private static final double FALL_DIVISOR = 1.2;
    private static final double CEILING = 0.5;
    private static final double CEILING_RESET = 0.2;

    @Override
    public void apply(Level level, LivingEntity user) {
        Vec3 motion = user.getDeltaMovement();
        double vertical = motion.y;
        if (vertical < 0.0) {
            vertical = vertical / FALL_DIVISOR + LIFT;
            user.fallDistance = user.fallDistance / FALL_DIVISOR;
        } else {
            vertical += LIFT;
        }
        if (vertical > CEILING) {
            vertical = CEILING_RESET;
        }
        user.setDeltaMovement(motion.x, vertical, motion.z);
    }
}
