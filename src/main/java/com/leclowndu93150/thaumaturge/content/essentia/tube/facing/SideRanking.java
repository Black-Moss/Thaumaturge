package com.leclowndu93150.thaumaturge.content.essentia.tube.facing;

import net.minecraft.core.Direction;

@FunctionalInterface
public interface SideRanking {
    int UNACCEPTABLE = Integer.MAX_VALUE;

    int rank(Direction side);
}
