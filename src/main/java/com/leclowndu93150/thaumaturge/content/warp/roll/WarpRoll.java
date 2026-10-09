package com.leclowndu93150.thaumaturge.content.warp.roll;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public record WarpRoll(ServerPlayer player, ServerLevel level, RandomSource random, int effectiveWarp, int normalWarp) {
    private static final int MAX_AMPLIFIER = 3;
    private static final int AMPLIFIER_STEP = 15;

    public int amplifier() {
        return Math.min(MAX_AMPLIFIER, effectiveWarp / AMPLIFIER_STEP);
    }

    public int scaled(int factor, int cap) {
        return Math.min(cap, factor * effectiveWarp);
    }
}
