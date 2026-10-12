package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainRolls;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GlintRoll;
import java.util.Optional;

public final class GlintCalculator {
    private static final float APPEAR_SHARE = 0.10F;
    private static final float ARRIVAL_SHARE = 0.10F;
    private static final float APPEAR_PEAK = 32.0F;
    private static final float ARRIVAL_PEAK = 16.0F;

    private GlintCalculator() {}

    public static Optional<GlintPlan> plan(int life, float remaining, GainRolls rolls) {
        if (life <= 0) {
            return Optional.empty();
        }
        float elapsed = life - remaining;
        float appearWindow = life * APPEAR_SHARE;
        if (elapsed < appearWindow) {
            return burst(APPEAR_PEAK * GainEasing.bell(elapsed / appearWindow), rolls.appear(), rolls);
        }
        float arrivalWindow = life * ARRIVAL_SHARE;
        if (remaining < arrivalWindow) {
            return burst(ARRIVAL_PEAK * GainEasing.bell(1.0F - remaining / arrivalWindow), rolls.arrival(), rolls);
        }
        return Optional.empty();
    }

    private static Optional<GlintPlan> burst(float size, GlintRoll roll, GainRolls rolls) {
        if (size <= 0.0F) {
            return Optional.empty();
        }
        return Optional.of(new GlintPlan(size, rolls.tiltDegrees() + roll.spinDegrees(), roll.frame(), roll.color()));
    }
}
