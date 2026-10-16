package com.leclowndu93150.thaumaturge.content.world.crystal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalShards {
    public static final int COUNT = 8;

    private static final int UNSUPPORTED_MASK = COUNT - 1;

    private CrystalShards() {}

    public static long seed(BlockState state, BlockPos pos) {
        return RandomSource.create(state.getSeed(pos)).nextLong();
    }

    public static List<Integer> order(Direction face, long seed) {
        List<Integer> order = new ArrayList<>(COUNT);
        for (int shard = 0; shard < COUNT; shard++) {
            order.add(shard);
        }
        Util.shuffle(order, RandomSource.create(seed + CrystalFaceTransforms.seedOffset(face)));
        return order;
    }

    public static int unsupported(long seed) {
        return (int) (seed & UNSUPPORTED_MASK);
    }

    public static boolean supports(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighbour = pos.relative(direction);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, direction.getOpposite());
    }
}
