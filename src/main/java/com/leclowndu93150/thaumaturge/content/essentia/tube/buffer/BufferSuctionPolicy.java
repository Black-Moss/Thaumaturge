package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class BufferSuctionPolicy {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int NOT_COUNTED = -1;
    private static final int BASE_SUCTION = 1;
    private static final int NO_SUCTION = 0;
    private static final int SUCTION_PER_BELLOWS = 32;

    private int bellows = NOT_COUNTED;

    public boolean needsCount() {
        return bellows == NOT_COUNTED;
    }

    public void recount(Level level, BlockPos pos) {
        bellows = BellowsHelper.countBellows(level, pos, DIRECTIONS);
    }

    public int bellowsCount() {
        return Math.max(bellows, NO_SUCTION);
    }

    public int offered(ChokeLevel choke) {
        return switch (choke) {
            case BLOCKED -> NO_SUCTION;
            case REDUCED -> BASE_SUCTION;
            case NORMAL -> bellows <= NO_SUCTION ? BASE_SUCTION : SUCTION_PER_BELLOWS * bellows;
        };
    }
}
