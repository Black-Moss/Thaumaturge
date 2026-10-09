package com.leclowndu93150.thaumaturge.content.warp.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public final class SpawnSpots {
    public static final int PLACEMENT_ATTEMPTS = 50;
    public static final float FULL_TURN_DEGREES = 360.0F;

    private static final int MIN_AXIS_SHIFT = 7;
    private static final int AXIS_SHIFT_SPREAD = 18;
    private static final int DIRECTION_CHOICES = 3;

    private SpawnSpots() {}

    public static BlockPos ringCell(BlockPos origin, RandomSource random) {
        return origin.offset(axisShift(random), axisShift(random), axisShift(random));
    }

    public static int symmetricSpread(RandomSource random, int bound) {
        return random.nextInt(bound) - random.nextInt(bound);
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

    private static int axisShift(RandomSource random) {
        return (MIN_AXIS_SHIFT + random.nextInt(AXIS_SHIFT_SPREAD)) * (random.nextInt(DIRECTION_CHOICES) - 1);
    }
}
