package com.leclowndu93150.thaumaturge.client.effect.instance.beam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BeamAnchor {
    private static final double SIDE_OFFSET_X = 0.066;
    private static final double SIDE_OFFSET_Z = 0.04;
    private static final double CHEST_LIFT = 0.25;
    private static final double CHEST_DROP = 0.06;
    private static final double LOOK_NUDGE = 0.3;
    private static final float FULL_TICK = 1.0F;

    private BeamAnchor() {}

    public static @Nullable Entity find(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(entityId);
        return entity != null && entity.isAlive() ? entity : null;
    }

    public static Vec3 castPoint(Entity caster, float partialTick) {
        Vec3 feet = caster.getPosition(partialTick);
        double yawRadians = caster.getYRot(partialTick) * Mth.DEG_TO_RAD;
        Vec3 look = caster.getViewVector(FULL_TICK);
        double x = feet.x - Math.cos(yawRadians) * SIDE_OFFSET_X + look.x * LOOK_NUDGE;
        double y = feet.y + caster.getBbHeight() / 2.0 + CHEST_LIFT - CHEST_DROP + look.y * LOOK_NUDGE;
        double z = feet.z - Math.sin(yawRadians) * SIDE_OFFSET_Z + look.z * LOOK_NUDGE;
        return new Vec3(x, y, z);
    }
}
