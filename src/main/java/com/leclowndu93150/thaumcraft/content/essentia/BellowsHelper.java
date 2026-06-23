package com.leclowndu93150.thaumcraft.content.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BellowsHelper {
    private BellowsHelper() {}

    public static int countBellows(Level level, BlockPos pos, Direction[] directions) {
        if (level == null) return 0;
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : directions) {
            cursor.setWithOffset(pos, dir);
            BlockEntity tile = level.getBlockEntity(cursor);
            if (tile instanceof IBellowsPower bellows
                    && bellows.bellowsFacing() == dir.getOpposite()
                    && bellows.bellowsEnabled()) {
                count++;
            }
        }
        return count;
    }
}
