package com.leclowndu93150.thaumaturge.content.golem;

import net.minecraft.util.Mth;

final class GolemWheel {
    private static final float WHEEL_CIRCUMFERENCE = 1.571F;
    private static final float FULL_TURN = 360.0F;
    private static final float QUARTER_TURN = 90.0F;

    private GolemWheel() {}

    static float spun(float angle, double deltaX, double deltaZ, float bodyYaw) {
        float turns = (float) Mth.length(deltaX, deltaZ) / WHEEL_CIRCUMFERENCE;
        float heading = (float) (Mth.atan2(deltaZ, deltaX) * Mth.RAD_TO_DEG) - QUARTER_TURN;
        return Mth.positiveModulo(angle + turns * (FULL_TURN - (bodyYaw - heading)), FULL_TURN);
    }
}
