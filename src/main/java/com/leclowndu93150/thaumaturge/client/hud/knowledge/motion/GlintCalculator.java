package com.leclowndu93150.thaumaturge.client.hud.knowledge.motion;

import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GainRolls;
import com.leclowndu93150.thaumaturge.client.hud.knowledge.entry.GlintRoll;
import java.util.Optional;
import net.minecraft.util.Mth;

public final class GlintCalculator {
    private static final float APPEAR_START_FRACTION = 0.9F;
    private static final float GLINT_SPAN_FRACTION = 0.1F;
    private static final float APPEAR_GLINT_SIZE = 16.0F;
    private static final float ARRIVAL_GLINT_SIZE = 8.0F;

    private GlintCalculator() {}

    public static Optional<GlintPlan> plan(int life, float remaining, GainRolls rolls) {
        float glintSpan = GLINT_SPAN_FRACTION * life;
        if (remaining > APPEAR_START_FRACTION * life) {
            return build(APPEAR_GLINT_SIZE, (life - remaining) / glintSpan, rolls.tiltDegrees(), rolls.appear());
        }
        if (remaining < glintSpan) {
            return build(ARRIVAL_GLINT_SIZE, 1.0F - remaining / glintSpan, rolls.tiltDegrees(), rolls.arrival());
        }
        return Optional.empty();
    }

    private static Optional<GlintPlan> build(float maxSize, float wave, int tilt, GlintRoll roll) {
        float size = maxSize * (1.0F - Mth.cos(Mth.TWO_PI * wave));
        if (size <= 0.0F) {
            return Optional.empty();
        }
        return Optional.of(new GlintPlan(size, tilt + roll.spinDegrees(), roll.frame(), roll.color()));
    }
}
