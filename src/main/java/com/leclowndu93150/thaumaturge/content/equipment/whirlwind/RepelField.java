package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RepelField implements WhirlwindStrategy {
    private static final double FIELD_REACH = 2.5;
    private static final double DISTANCE_SOFTENING = 0.1;

    @Override
    public void apply(Level level, LivingEntity user) {
        if (level.isClientSide()) {
            return;
        }
        AABB field = user.getBoundingBox().inflate(FIELD_REACH);
        Vec3 origin = user.position();
        Entity mount = user.getVehicle();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, field, candidate -> isRepelled(user, mount, candidate))) {
            shove(origin, target);
        }
    }

    private static boolean isRepelled(LivingEntity user, Entity mount, LivingEntity candidate) {
        return candidate != user && candidate != mount && !(candidate instanceof Player) && candidate.isAlive();
    }

    private static void shove(Vec3 origin, LivingEntity target) {
        Vec3 away = target.position().subtract(origin);
        double distance = away.length();
        target.push(away.scale(1.0 / (FIELD_REACH * (distance + DISTANCE_SOFTENING))));
    }
}
