package com.leclowndu93150.thaumaturge.client.effect.instance.beam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BeamAnchor {
    private static final double CHEST_HEIGHT_FRACTION = 0.6;
    private static final double HAND_DROP = 0.18;
    private static final double CHEST_FORWARD = 0.25;
    private static final double HAND_SIDEWAYS = 0.32;
    private static final double LOOK_NUDGE = 0.2;

    private BeamAnchor() {}

    public static @Nullable LivingEntity find(int entityId) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(entityId);
        if (entity instanceof LivingEntity living && living.isAlive()) {
            return living;
        }
        return null;
    }

    public static Vec3 castPoint(LivingEntity caster, float partialTick) {
        Vec3 feet = caster.getPosition(partialTick);
        double yawRadians = caster.getViewYRot(partialTick) * Mth.DEG_TO_RAD;
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double side = handSide(caster) == HumanoidArm.RIGHT ? HAND_SIDEWAYS : -HAND_SIDEWAYS;
        double rightX = -forwardZ;
        double rightZ = forwardX;
        Vec3 look = caster.getViewVector(partialTick);
        double x = feet.x + forwardX * CHEST_FORWARD + rightX * side + look.x * LOOK_NUDGE;
        double y = feet.y + caster.getBbHeight() * CHEST_HEIGHT_FRACTION - HAND_DROP + look.y * LOOK_NUDGE;
        double z = feet.z + forwardZ * CHEST_FORWARD + rightZ * side + look.z * LOOK_NUDGE;
        return new Vec3(x, y, z);
    }

    private static HumanoidArm handSide(LivingEntity caster) {
        HumanoidArm main = caster.getMainArm();
        return caster.isUsingItem() && caster.getUsedItemHand() == InteractionHand.OFF_HAND ? main.getOpposite() : main;
    }
}
