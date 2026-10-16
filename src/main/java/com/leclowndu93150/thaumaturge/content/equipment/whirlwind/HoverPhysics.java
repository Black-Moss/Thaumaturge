package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HoverPhysics implements WhirlwindStrategy {
    private static final double DESCENT_DIVISOR = 1.2;
    private static final double MAX_RISE_PER_TICK = 0.5;
    private static final double RISE_AFTER_OVERSHOOT = 0.2;

    @Override
    public void apply(Level level, LivingEntity user) {
        Vec3 motion = user.getDeltaMovement();
        double vertical = motion.y;
        if (vertical < 0) {
            vertical /= DESCENT_DIVISOR;
            user.fallDistance /= DESCENT_DIVISOR;
        }
        vertical += user.getGravity();
        if (vertical > MAX_RISE_PER_TICK) {
            vertical = RISE_AFTER_OVERSHOOT;
        }
        user.setDeltaMovement(motion.x, vertical, motion.z);
    }
}
