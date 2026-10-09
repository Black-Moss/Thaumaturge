package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

final class NodeBiomeSpread {
    private static final int TAINT_CONVERSION_INTERVAL = 100;
    private static final int TAINT_CONVERSION_ODDS = 500;
    private static final int SPREAD_INTERVAL = 50;
    private static final int TAINTED_RANGE = 8;
    private static final int DARK_RANGE = 12;
    private static final int PURE_RANGE = 8;
    private static final int SILVERWOOD_RADIUS = 1;

    private NodeBiomeSpread() {}

    static void tick(ServerLevel level, BlockEntityNode node, BlockPos pos, int counter) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        RandomSource random = level.getRandom();
        NodeType type = node.kind();
        if (counter % TAINT_CONVERSION_INTERVAL == 0 && type != NodeType.PURE && type != NodeType.TAINTED && TaintBiomeManager.isTainted(level, pos) && random.nextInt(TAINT_CONVERSION_ODDS) == 0) {
            node.reclassify(NodeType.TAINTED);
            node.invalidateRefill();
            type = NodeType.TAINTED;
        }
        if (counter % SPREAD_INTERVAL != 0) {
            return;
        }
        switch (type) {
            case TAINTED -> taint(level, sample(pos, TAINTED_RANGE, random));
            case DARK -> eerie(level, sample(pos, DARK_RANGE, random));
            case PURE -> purify(level, pos, sample(pos, PURE_RANGE, random));
            case NORMAL, UNSTABLE, HUNGRY -> {
            }
        }
    }

    private static BlockPos sample(BlockPos origin, int range, RandomSource random) {
        return origin.offset(random.nextInt(range) - random.nextInt(range), 0, random.nextInt(range) - random.nextInt(range));
    }

    private static void taint(ServerLevel level, BlockPos sample) {
        if (level.hasChunkAt(sample)) {
            TaintBiomeManager.taintColumn(level, sample);
        }
    }

    private static void eerie(ServerLevel level, BlockPos sample) {
        if (level.hasChunkAt(sample) && !level.getBiome(sample).is(TTBiomes.EERIE)) {
            TaintBiomeManager.replaceColumn(level, sample, TTBiomes.EERIE);
        }
    }

    private static void purify(ServerLevel level, BlockPos origin, BlockPos sample) {
        if (!level.hasChunkAt(sample)) {
            return;
        }
        if (TaintBiomeManager.isTainted(level, sample) || silverwoodNearby(level, origin)) {
            TaintBiomeManager.replaceColumn(level, sample, TTBiomes.MAGICAL_FOREST);
        }
    }

    private static boolean silverwoodNearby(ServerLevel level, BlockPos origin) {
        for (BlockPos cell : BlockPos.betweenClosed(origin.offset(-SILVERWOOD_RADIUS, -SILVERWOOD_RADIUS, -SILVERWOOD_RADIUS),
                origin.offset(SILVERWOOD_RADIUS, SILVERWOOD_RADIUS, SILVERWOOD_RADIUS))) {
            if (level.hasChunkAt(cell) && level.getBlockState(cell).is(TTBlockTags.SILVERWOOD_LOGS)) {
                return true;
            }
        }
        return false;
    }
}
