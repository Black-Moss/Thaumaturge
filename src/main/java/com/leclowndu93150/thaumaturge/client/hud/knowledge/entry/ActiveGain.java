package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

public final class ActiveGain {
    private final GainEntry entry;
    private int remaining;

    public ActiveGain(GainEntry entry) {
        this.entry = entry;
        this.remaining = entry.life();
    }

    public GainEntry entry() {
        return entry;
    }

    public float remainingAt(float partialTick) {
        return Math.max(0.0F, remaining - partialTick);
    }

    boolean expireAfterTick() {
        remaining--;
        return remaining <= 0;
    }
}
