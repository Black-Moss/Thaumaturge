package com.leclowndu93150.thaumaturge.content.golem;

import net.minecraft.util.Mth;

final class GolemFacing {
    private GolemFacing() {}

    static float yawToward(double deltaX, double deltaZ) {
        return -(float) (Mth.atan2(deltaX, deltaZ) * Mth.RAD_TO_DEG);
    }

    static void face(EntityThaumaturgeGolem golem, double deltaX, double deltaZ) {
        float yaw = yawToward(deltaX, deltaZ);
        golem.setYRot(yaw);
        golem.yBodyRot = yaw;
    }
}
