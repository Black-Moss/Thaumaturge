package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;

public final class ShieldDepletionTracker {
    private static final int EMPTY = 0;

    private ShieldDepletionTracker() {}

    public static void observe(RunicShieldState state, int shield, long now) {
        boolean justDepleted = shield <= EMPTY && state.lastCharge > EMPTY;
        if (justDepleted) {
            state.nextCycle = now + ThaumaturgeCommonConfig.SHIELD_WAIT.get();
        }
        state.lastCharge = shield;
    }
}
