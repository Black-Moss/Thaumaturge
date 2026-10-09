package com.leclowndu93150.thaumaturge.content.essentia.tube.facing;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class SideRotation {
    private static final Direction[] DIRECTIONS = Direction.values();

    private SideRotation() {}

    public static @Nullable Direction next(Direction current, SideRanking ranking) {
        Direction best = null;
        int bestRank = SideRanking.UNACCEPTABLE;
        for (int step = 1; step < DIRECTIONS.length; step++) {
            Direction candidate = DIRECTIONS[(current.ordinal() + step) % DIRECTIONS.length];
            int rank = ranking.rank(candidate);
            if (rank < bestRank) {
                best = candidate;
                bestRank = rank;
            }
        }
        return best;
    }
}
