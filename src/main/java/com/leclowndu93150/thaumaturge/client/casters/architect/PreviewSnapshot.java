package com.leclowndu93150.thaumaturge.client.casters.architect;

import java.util.List;
import net.minecraft.core.BlockPos;

public final class PreviewSnapshot {
    public static final PreviewSnapshot EMPTY = new PreviewSnapshot(List.of(), new int[0]);

    private final List<BlockPos> positions;
    private final int[] neighbourMasks;

    PreviewSnapshot(List<BlockPos> positions, int[] neighbourMasks) {
        this.positions = positions;
        this.neighbourMasks = neighbourMasks;
    }

    public boolean isEmpty() {
        return positions.isEmpty();
    }

    public int size() {
        return positions.size();
    }

    public BlockPos position(int index) {
        return positions.get(index);
    }

    public int neighbourMask(int index) {
        return neighbourMasks[index];
    }
}
