package com.leclowndu93150.thaumaturge.client.casters.architect;

import net.minecraft.core.Direction;

public final class CubeCorner {
    public static final int COUNT = 8;
    private static final int X_POSITIVE_BIT = 1;
    private static final int Z_POSITIVE_BIT = 2;
    private static final int DOWN_FROM = 4;
    private static final CubeCorner[] ALL = build();

    private final int signX;
    private final int signY;
    private final int signZ;
    private final int blockers;

    private CubeCorner(int signX, int signY, int signZ) {
        this.signX = signX;
        this.signY = signY;
        this.signZ = signZ;
        this.blockers = NeighbourMask.bit(signX, 0, 0) | NeighbourMask.bit(0, 0, signZ) | NeighbourMask.bit(0, signY, 0);
    }

    private static CubeCorner[] build() {
        CubeCorner[] corners = new CubeCorner[COUNT];
        for (int index = 0; index < COUNT; index++) {
            int signX = (index & X_POSITIVE_BIT) == 0 ? -1 : 1;
            int signZ = (index & Z_POSITIVE_BIT) == 0 ? -1 : 1;
            int signY = index < DOWN_FROM ? 1 : -1;
            corners[index] = new CubeCorner(signX, signY, signZ);
        }
        return corners;
    }

    public static CubeCorner get(int index) {
        return ALL[index];
    }

    public int signAlong(Direction direction) {
        return direction.getStepX() * signX + direction.getStepY() * signY + direction.getStepZ() * signZ;
    }

    public boolean isOpen(int neighbourMask) {
        return (neighbourMask & blockers) == 0;
    }
}
