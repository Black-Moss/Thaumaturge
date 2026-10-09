package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class RepelField implements WhirlwindStrategy {
    private static final double RADIUS = 2.5;
    private static final double FALLOFF_SOFTENING = 0.1;

    @Override
    public void apply(Level level, LivingEntity user) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(RADIUS), entity -> isRepelled(user, entity))) {
            Vec3 offset = target.position().subtract(user.position());
            Vec3 velocityChange = offset.normalize().scale(strengthAt(offset.length()));
            target.push(velocityChange.x, velocityChange.y, velocityChange.z);
        }
    }

    private static double strengthAt(double distance) {
        return distance / (distance + FALLOFF_SOFTENING) / RADIUS;
    }

    private static boolean isRepelled(LivingEntity user, LivingEntity entity) {
        return entity != user && !(entity instanceof Player) && entity.isAlive() && entity != user.getVehicle();
    }
}
