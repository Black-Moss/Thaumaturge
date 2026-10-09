package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class NeighbourRing {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int LAYERS = 2;

    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private Direction side = Direction.NORTH;

    public int slots() {
        return LAYERS * DIRECTIONS.length;
    }

    public boolean aim(BlockPos origin, Direction front, int slot) {
        Direction candidate = DIRECTIONS[slot % DIRECTIONS.length];
        if (candidate == Direction.DOWN || candidate == front) {
            return false;
        }
        int layer = slot / DIRECTIONS.length;
        side = candidate;
        cursor.setWithOffset(origin, candidate.getStepX(), candidate.getStepY() + layer, candidate.getStepZ());
        return true;
    }

    public BlockPos pos() {
        return cursor;
    }

    public Direction side() {
        return side;
    }
}
