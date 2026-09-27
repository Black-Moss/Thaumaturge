package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

public final class TaintSplosion {
    private static final int ATTEMPTS = 10;
    private static final float SPREAD = 6.0F;
    private static final float PRESSURE = 0.01F;

    private TaintSplosion() {}

    public static void burst(ServerLevel level, BlockPos center, RandomSource random) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = center.getX() + (int) ((random.nextFloat() - random.nextFloat()) * SPREAD);
            int z = center.getZ() + (int) ((random.nextFloat() - random.nextFloat()) * SPREAD);
            if (!random.nextBoolean()) {
                continue;
            }
            BlockPos column = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (!level.hasChunkAt(column) || !TaintBiomeManager.taintColumn(level, column)) {
                continue;
            }
            if (level.getBlockState(column).canBeReplaced() && level.getBlockState(column).getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, column)) {
                level.setBlock(column, BlockTaintFibre.stateForWorld(level, column), Block.UPDATE_ALL);
            }
            TaintEcology.addPressure(level, column, PRESSURE);
        }
    }
}
