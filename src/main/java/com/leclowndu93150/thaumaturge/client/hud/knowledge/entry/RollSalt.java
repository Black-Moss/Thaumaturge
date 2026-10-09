package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import it.unimi.dsi.fastutil.HashCommon;

public enum RollSalt {
    TILT(0, 12), START_X(1, 32), START_Y(2, 32), END_X(3, 8), END_Y(4, 8), APPEAR_FRAME(5, 16), APPEAR_SPIN(6, 360), APPEAR_GREEN(7, 67), APPEAR_BLUE(8, 192), ARRIVAL_FRAME(9, 16), ARRIVAL_SPIN(10,
            360), ARRIVAL_GREEN(11, 67), ARRIVAL_BLUE(12, 192);

    private final int salt;
    private final long range;

    RollSalt(int salt, int range) {
        this.salt = salt;
        this.range = range;
    }

    public int roll(long seed) {
        return (int) Math.floorMod(HashCommon.mix(seed + salt), range);
    }
}
