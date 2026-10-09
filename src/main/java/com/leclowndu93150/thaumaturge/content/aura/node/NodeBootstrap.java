package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class NodeBootstrap {
    private static final float FLUX = 100.0F;
    private static final int ATTEMPTS = 16;
    private static final int SPREAD = 16;

    private NodeBootstrap() {}

    static void run(ServerLevel level, BlockPos pos) {
        AuraHelper.polluteAura(level, pos, FLUX, false);
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos target = pos.offset(offset(random), offset(random), offset(random));
            if (!columnsGenerated(level, target)) {
                continue;
            }
            BlockState existing = level.getBlockState(target);
            if ((existing.isAir() || existing.canBeReplaced()) && TaintHelper.hasSturdyNeighbour(level, target)) {
                level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
            }
        }
    }

    private static int offset(RandomSource random) {
        return random.nextInt(SPREAD) - random.nextInt(SPREAD);
    }

    static boolean columnsGenerated(LevelReader reader, BlockPos pos) {
        return reader.hasChunkAt(pos) && reader.hasChunkAt(pos.north()) && reader.hasChunkAt(pos.south()) && reader.hasChunkAt(pos.east()) && reader.hasChunkAt(pos.west());
    }
}
