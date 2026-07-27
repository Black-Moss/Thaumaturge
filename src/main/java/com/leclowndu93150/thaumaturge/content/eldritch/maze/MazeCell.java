package com.leclowndu93150.thaumaturge.content.eldritch.maze;

public final class MazeCell {
    public static final int FEATURE_PORTAL = 1;
    public static final int FEATURE_BOSS_NW = 2;
    public static final int FEATURE_BOSS_NE = 3;
    public static final int FEATURE_BOSS_SW = 4;
    public static final int FEATURE_BOSS_SE = 5;
    public static final int FEATURE_KEY = 6;
    public static final int FEATURE_NEST = 7;
    public static final int FEATURE_LIBRARY = 8;

    private static final int BIT_NORTH = 1;
    private static final int BIT_SOUTH = 2;
    private static final int BIT_EAST = 4;
    private static final int BIT_WEST = 8;
    private static final int BIT_ABOVE = 16;
    private static final int BIT_BELOW = 32;
    private static final int FEATURE_SHIFT = 8;

    public boolean north;
    public boolean south;
    public boolean east;
    public boolean west;
    public boolean above;
    public boolean below;
    public byte feature;

    public MazeCell() {}

    public MazeCell(short data) {
        north = (data & BIT_NORTH) != 0;
        south = (data & BIT_SOUTH) != 0;
        east = (data & BIT_EAST) != 0;
        west = (data & BIT_WEST) != 0;
        above = (data & BIT_ABOVE) != 0;
        below = (data & BIT_BELOW) != 0;
        feature = (byte) (data >> FEATURE_SHIFT);
    }

    public short pack() {
        int out = 0;
        if (north) {
            out |= BIT_NORTH;
        }
        if (south) {
            out |= BIT_SOUTH;
        }
        if (east) {
            out |= BIT_EAST;
        }
        if (west) {
            out |= BIT_WEST;
        }
        if (above) {
            out |= BIT_ABOVE;
        }
        if (below) {
            out |= BIT_BELOW;
        }
        out |= feature << FEATURE_SHIFT;
        return (short) out;
    }

    public boolean hasAnyOpening() {
        return north || south || east || west;
    }

    public int openingCount() {
        return (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);
    }
}
