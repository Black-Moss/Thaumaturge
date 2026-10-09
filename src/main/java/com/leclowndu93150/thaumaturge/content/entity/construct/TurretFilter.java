package com.leclowndu93150.thaumaturge.content.entity.construct;

public enum TurretFilter {
    ANIMALS(1), MOBS(2), PLAYERS(4), FRIENDLY(8);

    private final byte mask;

    TurretFilter(int mask) {
        this.mask = (byte) mask;
    }

    public byte mask() {
        return mask;
    }
}
