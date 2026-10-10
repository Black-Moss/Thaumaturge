package com.leclowndu93150.thaumaturge.content.warp.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public final class SpawnSpots {
    public static final int PLACEMENT_ATTEMPTS = 50;
    public static final float FULL_TURN_DEGREES = 360.0F;
    public static final double CELL_CENTER = 0.5;

    private static final int STILL_AXIS_ODDS = 3;
    private static final int NEAREST_SHIFT = 7;
    private static final int FARTHEST_SHIFT = 24;

    private SpawnSpots() {}

    public static BlockPos ringCell(BlockPos origin, RandomSource random) {
        int dx;
        int dz;
        do {
            dx = displacement(random);
            dz = displacement(random);
        } while (dx == 0 && dz == 0);
        return origin.offset(dx, displacement(random), dz);
    }

    private static int displacement(RandomSource random) {
        if (random.nextInt(STILL_AXIS_ODDS) == 0) {
            return 0;
        }
        int distance = Mth.nextInt(random, NEAREST_SHIFT, FARTHEST_SHIFT);
        return random.nextBoolean() ? distance : -distance;
    }

    public static boolean standsOnFullBlock(ServerLevel level, BlockPos cell) {
        BlockPos below = cell.below();
        return level.getBlockState(below).isCollisionShapeFullBlock(level, below);
    }

    public static boolean standsOnSolidRender(ServerLevel level, BlockPos cell) {
        BlockPos below = cell.below();
        return level.getBlockState(below).isSolidRender();
    }

    public static boolean hasFreeSpace(ServerLevel level, Entity entity) {
        return level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox());
    }
}
