package com.leclowndu93150.thaumaturge.client.effect.instance.beam;

import com.leclowndu93150.thaumaturge.client.effect.instance.BeamPayloadIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BeamAnchor {
    private static final double CHEST_LIFT = 0.25;
    private static final double HAND_SIDE_SHIFT = 0.066;
    private static final double HAND_FORWARD_SHIFT = 0.04;
    private static final double HAND_DROP = 0.06;
    private static final double LOOK_REACH = 0.3;
    private static final double HALF = 2.0;
    private static final float LOOK_PARTIAL_TICK = 1.0F;

    private BeamAnchor() {}

    public static @Nullable LivingEntity find(int entityId) {
        if (entityId == BeamPayloadIds.NO_ENTITY) {
            return null;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(entityId);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    public static Vec3 chestPoint(LivingEntity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / HALF + CHEST_LIFT, entity.getZ());
    }

    public static HandSpan handSpan(LivingEntity entity, float partialTick) {
        Vec3 previous = handPoint(entity, entity.xo, entity.yo, entity.zo, entity.yRotO);
        Vec3 current = handPoint(entity, entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(partialTick));
        return new HandSpan(previous, current);
    }

    private static Vec3 handPoint(LivingEntity entity, double x, double y, double z, float yawDegrees) {
        double yawRadians = Math.toRadians(yawDegrees);
        Vec3 look = entity.getViewVector(LOOK_PARTIAL_TICK).scale(LOOK_REACH);
        double handX = x - Math.cos(yawRadians) * HAND_SIDE_SHIFT + look.x;
        double handY = y + entity.getBbHeight() / HALF + CHEST_LIFT - HAND_DROP + look.y;
        double handZ = z - Math.sin(yawRadians) * HAND_FORWARD_SHIFT + look.z;
        return new Vec3(handX, handY, handZ);
    }

    public record HandSpan(Vec3 previous, Vec3 current) {
        public Vec3 lerp(float partialTick) {
            return this.previous.lerp(this.current, partialTick);
        }
    }
}
