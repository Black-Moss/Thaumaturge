package com.leclowndu93150.thaumaturge.content.equipment.whirlwind;

public final class PulseScheduler {
    private final int interval;

    public PulseScheduler(int interval) {
        this.interval = interval;
    }

    public boolean isDue(long tick) {
        return tick % interval == 0;
    }
}
