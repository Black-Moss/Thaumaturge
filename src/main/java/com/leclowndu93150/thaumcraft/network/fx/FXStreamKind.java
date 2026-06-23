package com.leclowndu93150.thaumcraft.network.fx;

public enum FXStreamKind {
    ARC,
    BOLT,
    BEAM,
    ESSENTIA,
    BORE,
    VOID;

    private static final FXStreamKind[] BY_ORDINAL = values();

    public static FXStreamKind byOrdinal(int ordinal) {
        return BY_ORDINAL[ordinal];
    }
}
