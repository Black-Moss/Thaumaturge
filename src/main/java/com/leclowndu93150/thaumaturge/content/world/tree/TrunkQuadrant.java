package com.leclowndu93150.thaumaturge.content.world.tree;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum TrunkQuadrant implements StringRepresentable {
    SINGLE("single", 0, 0), NORTH_WEST("nw", 0, 0), NORTH_EAST("ne", 1, 0), SOUTH_WEST("sw", 0, 1), SOUTH_EAST("se", 1, 1);

    public static final int TRUNK_WIDTH = 2;
    public static final EnumProperty<TrunkQuadrant> PROPERTY = EnumProperty.create("quadrant", TrunkQuadrant.class);

    private static final int LAST_OFFSET = TRUNK_WIDTH - 1;

    private final String serializedName;
    private final int eastOffset;
    private final int southOffset;

    TrunkQuadrant(String serializedName, int eastOffset, int southOffset) {
        this.serializedName = serializedName;
        this.eastOffset = eastOffset;
        this.southOffset = southOffset;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public int eastOffset() {
        return eastOffset;
    }

    public int southOffset() {
        return southOffset;
    }

    public boolean isCorner() {
        return this != SINGLE;
    }

    public static TrunkQuadrant forOffsets(int eastOffset, int southOffset, int trunkWidth) {
        if (trunkWidth != TRUNK_WIDTH || eastOffset < 0 || eastOffset > LAST_OFFSET || southOffset < 0 || southOffset > LAST_OFFSET) {
            return SINGLE;
        }
        if (southOffset == 0) {
            return eastOffset == 0 ? NORTH_WEST : NORTH_EAST;
        }
        return eastOffset == 0 ? SOUTH_WEST : SOUTH_EAST;
    }

    public TrunkQuadrant rotated(Rotation rotation) {
        if (!isCorner()) {
            return this;
        }
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> forOffsets(LAST_OFFSET - southOffset, eastOffset, TRUNK_WIDTH);
            case CLOCKWISE_180 -> forOffsets(LAST_OFFSET - eastOffset, LAST_OFFSET - southOffset, TRUNK_WIDTH);
            case COUNTERCLOCKWISE_90 -> forOffsets(southOffset, LAST_OFFSET - eastOffset, TRUNK_WIDTH);
        };
    }

    public TrunkQuadrant mirrored(Mirror mirror) {
        if (!isCorner()) {
            return this;
        }
        return switch (mirror) {
            case NONE -> this;
            case LEFT_RIGHT -> forOffsets(eastOffset, LAST_OFFSET - southOffset, TRUNK_WIDTH);
            case FRONT_BACK -> forOffsets(LAST_OFFSET - eastOffset, southOffset, TRUNK_WIDTH);
        };
    }
}
