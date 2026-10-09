package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

final class ReservoirPockets {
    private static final int CANDIDATE_COUNT = 50;
    private static final int SPREAD_BOUND = 5;

    private ReservoirPockets() {}

    static void scatter(ServerLevel level, BlockPos origin, int budget) {
        RandomSource random = level.getRandom();
        List<BlockPos> candidates = collectCandidates(origin, random);
        int placed = 0;
        for (BlockPos target : candidates) {
            if (placed >= budget) {
                return;
            }
            if (placePocket(level, origin, target)) {
                placed++;
            }
        }
    }

    private static List<BlockPos> collectCandidates(BlockPos origin, RandomSource random) {
        List<BlockPos> candidates = new ArrayList<>(CANDIDATE_COUNT);
        for (int index = 0; index < CANDIDATE_COUNT; index++) {
            int dz = offset(random);
            int dy = offset(random);
            int dx = offset(random);
            candidates.add(origin.offset(dx, dy, dz));
        }
        return candidates;
    }

    private static int offset(RandomSource random) {
        int back = random.nextInt(SPREAD_BOUND);
        int forward = random.nextInt(SPREAD_BOUND);
        return forward - back;
    }

    private static boolean placePocket(ServerLevel level, BlockPos origin, BlockPos target) {
        if (!level.hasChunkAt(target) || !level.getBlockState(target).isAir()) {
            return false;
        }
        if (target.getY() < origin.getY()) {
            return PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA);
        }
        return PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
    }
}
