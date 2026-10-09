package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.content.essentia.cadence.IntervalTimer;

public final class ThaumatoriumScheduler {
    private static final int REFRESH_TICKS = 40;
    private static final int WORK_TICKS = 5;

    private final IntervalTimer refreshTimer = IntervalTimer.firingImmediately(REFRESH_TICKS);
    private final IntervalTimer workTimer = IntervalTimer.firingImmediately(WORK_TICKS);
    private boolean refreshDue;
    private boolean workDue;

    public void advance() {
        refreshDue = refreshTimer.advance();
        workDue = workTimer.advance();
    }

    public boolean refreshDue() {
        return refreshDue;
    }

    public boolean workDue() {
        return workDue;
    }
}
