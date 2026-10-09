package com.leclowndu93150.thaumaturge.content.essentia.cadence;

public final class IntervalTimer {
    private static final int FIRES_NOW = 0;
    private static final int ONE_TICK = 1;

    private final int period;
    private int remaining;

    private IntervalTimer(int period, int remaining) {
        if (period < ONE_TICK) {
            throw new IllegalArgumentException("Interval period must be at least " + ONE_TICK);
        }
        this.period = period;
        this.remaining = remaining;
    }

    public static IntervalTimer firingImmediately(int period) {
        return new IntervalTimer(period, FIRES_NOW);
    }

    public static IntervalTimer firingAfterFullPeriod(int period) {
        return new IntervalTimer(period, period - ONE_TICK);
    }

    public boolean advance() {
        if (remaining <= FIRES_NOW) {
            remaining = period - ONE_TICK;
            return true;
        }
        remaining--;
        return false;
    }
}
