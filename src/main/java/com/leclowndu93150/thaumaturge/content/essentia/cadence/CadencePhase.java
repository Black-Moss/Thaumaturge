package com.leclowndu93150.thaumaturge.content.essentia.cadence;

public record CadencePhase(int period) {
    private static final int MIN_PERIOD = 1;

    public CadencePhase {
        if (period < MIN_PERIOD) {
            throw new IllegalArgumentException("Cadence period must be at least " + MIN_PERIOD);
        }
    }

    public static CadencePhase every(int period) {
        return new CadencePhase(period);
    }

    public boolean isDue(int ticks) {
        return ticks % period == 0;
    }
}
