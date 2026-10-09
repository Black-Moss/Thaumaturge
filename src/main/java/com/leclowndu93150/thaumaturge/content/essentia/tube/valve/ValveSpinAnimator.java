package com.leclowndu93150.thaumaturge.content.essentia.tube.valve;

import net.minecraft.util.Mth;

public final class ValveSpinAnimator {
    private static final float STEP_DEGREES = 20.0F;
    private static final float OPEN_ANGLE = 0.0F;
    private static final float CLOSED_ANGLE = 360.0F;

    private float angle;
    private float previousAngle;
    private float target = OPEN_ANGLE;

    public void aim(boolean open) {
        target = open ? OPEN_ANGLE : CLOSED_ANGLE;
    }

    public void tick() {
        previousAngle = angle;
        if (angle < target) {
            angle = Math.min(angle + STEP_DEGREES, target);
        } else if (angle > target) {
            angle = Math.max(angle - STEP_DEGREES, target);
        }
    }

    public float angle() {
        return angle;
    }

    public float angle(float partialTick) {
        return Mth.lerp(partialTick, previousAngle, angle);
    }
}
