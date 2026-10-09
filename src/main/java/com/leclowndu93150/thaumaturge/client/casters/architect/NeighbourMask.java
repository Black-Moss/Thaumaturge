package com.leclowndu93150.thaumaturge.client.casters.architect;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

final class NeighbourMask {
    private static final int OFFSET_MIN = -1;
    private static final int OFFSET_MAX = 1;
    private static final int OFFSET_SPAN = 3;

    private NeighbourMask() {}

    static int bit(int dx, int dy, int dz) {
        int index = ((dx - OFFSET_MIN) * OFFSET_SPAN + (dy - OFFSET_MIN)) * OFFSET_SPAN + (dz - OFFSET_MIN);
        return 1 << index;
    }

    static int of(LongSet members, BlockPos pos) {
        int mask = 0;
        for (int dx = OFFSET_MIN; dx <= OFFSET_MAX; dx++) {
            for (int dy = OFFSET_MIN; dy <= OFFSET_MAX; dy++) {
                for (int dz = OFFSET_MIN; dz <= OFFSET_MAX; dz++) {
                    if ((dx != 0 || dy != 0 || dz != 0) && members.contains(BlockPos.asLong(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz))) {
                        mask |= bit(dx, dy, dz);
                    }
                }
            }
        }
        return mask;
    }

    static boolean has(int mask, Direction direction) {
        return (mask & bit(direction.getStepX(), direction.getStepY(), direction.getStepZ())) != 0;
    }
}
