package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import net.minecraft.util.Mth;

public enum ChokeLevel {
    NORMAL, REDUCED, BLOCKED;

    private static final ChokeLevel[] LEVELS = values();

    public static ChokeLevel fromOrdinal(int ordinal) {
        return LEVELS[Mth.clamp(ordinal, NORMAL.ordinal(), BLOCKED.ordinal())];
    }

    public ChokeLevel next() {
        return LEVELS[(ordinal() + 1) % LEVELS.length];
    }
}
