package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.warp.PlayerWarpState;

public record WarpReading(int temporary, int normal, int permanent, int gear, int counter) {
    private static final int PERCENT = 100;
    private static final int EFFECTIVE_CAP = 100;
    private static final int RAW_SHARE = 2;
    private static final int SHARES = 3;
    private static final double SMALLEST_DROP = 5.0;
    private static final double ROOT_WEIGHT = 2.0;
    private static final double GEAR_RELIEF = 2.0;

    public static WarpReading of(PlayerWarpState state, int gear) {
        return new WarpReading(state.get(WarpType.TEMPORARY), state.get(WarpType.NORMAL), state.get(WarpType.PERMANENT), gear, state.getCounter());
    }

    public long raw() {
        return (long) temporary + normal + permanent + gear;
    }

    public int actual() {
        return normal + permanent;
    }

    public boolean primed() {
        return counter > 0 && raw() > 0;
    }

    public double fireChance() {
        return Math.min(1.0, (Math.floor(Math.sqrt(counter)) + 1.0) / PERCENT);
    }

    public int effective() {
        long weighted = Math.floorDiv(RAW_SHARE * raw() + counter, (long) SHARES);
        return (int) Math.min(EFFECTIVE_CAP, weighted);
    }

    public int counterAfterEvent() {
        double floorCase = counter - SMALLEST_DROP;
        double rootCase = counter - ROOT_WEIGHT * Math.sqrt(counter) + GEAR_RELIEF * gear;
        return (int) Math.min(floorCase, rootCase);
    }
}
