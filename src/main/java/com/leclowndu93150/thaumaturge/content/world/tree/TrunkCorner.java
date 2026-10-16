package com.leclowndu93150.thaumaturge.content.world.tree;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum TrunkCorner implements StringRepresentable {
    NONE("none", 0, 0), NORTH_WEST("north_west", 0, 0), NORTH_EAST("north_east", 1, 0), SOUTH_WEST("south_west", 0, 1), SOUTH_EAST("south_east", 1, 1);

    public static final EnumProperty<TrunkCorner> PROPERTY = EnumProperty.create("trunk", TrunkCorner.class);
    private static final int SPAN = 2;

    private final String name;
    private final int offsetX;
    private final int offsetZ;

    TrunkCorner(String name, int offsetX, int offsetZ) {
        this.name = name;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public static TrunkCorner atOffset(int offsetX, int offsetZ, int trunkWidth) {
        if (trunkWidth != SPAN) {
            return NONE;
        }
        for (TrunkCorner corner : values()) {
            if (corner != NONE && corner.offsetX == offsetX && corner.offsetZ == offsetZ) {
                return corner;
            }
        }
        return NONE;
    }

    public TrunkCorner rotate(Rotation rotation) {
        if (this == NONE) {
            return NONE;
        }
        return switch (rotation) {
            case NONE -> this;
            case CLOCKWISE_90 -> atOffset(SPAN - 1 - offsetZ, offsetX, SPAN);
            case CLOCKWISE_180 -> atOffset(SPAN - 1 - offsetX, SPAN - 1 - offsetZ, SPAN);
            case COUNTERCLOCKWISE_90 -> atOffset(offsetZ, SPAN - 1 - offsetX, SPAN);
        };
    }

    public TrunkCorner mirror(Mirror mirror) {
        if (this == NONE) {
            return NONE;
        }
        return switch (mirror) {
            case NONE -> this;
            case LEFT_RIGHT -> atOffset(offsetX, SPAN - 1 - offsetZ, SPAN);
            case FRONT_BACK -> atOffset(SPAN - 1 - offsetX, offsetZ, SPAN);
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
