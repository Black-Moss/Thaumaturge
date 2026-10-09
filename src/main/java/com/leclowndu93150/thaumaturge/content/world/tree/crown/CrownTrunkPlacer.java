package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class CrownTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<CrownTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance).and(CrownShape.CODEC.fieldOf("shape").forGetter(placer -> placer.shape))
            .and(CrownRule.CODEC.fieldOf("rule").forGetter(placer -> placer.rule)).apply(instance, CrownTrunkPlacer::new));

    private static final int MIN_TREE_HEIGHT = 6;
    private static final int INVALID_HEIGHT = -1;
    private static final int TOP_CLUSTER_DROP = 4;
    private static final int CLUSTER_CLEARANCE = 4;
    private static final double CROWN_FLOOR_FRACTION = 0.3;
    private static final double LIMB_FLOOR_FRACTION = 0.2;
    private static final double STACKED_CROWN_WIDTH = 1.66;
    private static final double LAYER_BASE = 1.382;
    private static final double LAYER_HEIGHT_FACTOR = 0.9;
    private static final double LAYER_HEIGHT_DIVISOR = 13.0;
    private static final double DISTANCE_MIN = 0.328;
    private static final double SHRINK_FACTOR = 0.5;

    private final CrownShape shape;
    private final CrownRule rule;

    public CrownTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, CrownShape shape, CrownRule rule) {
        super(baseHeight, heightRandA, heightRandB);
        this.shape = shape;
        this.rule = rule;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TTTreePlacers.CROWN_TRUNK.get();
    }

    @Override
    public int getTreeHeight(RandomSource random) {
        int height = baseHeight + random.nextInt(heightRandA + 1);
        if (heightRandB > 0) {
            height += random.nextInt(heightRandB + 1);
        }
        return height;
    }

    @Override
    public boolean isFree(WorldGenLevel level, BlockPos pos) {
        return true;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int treeHeight, BlockPos origin, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> attachments = new ArrayList<>();
        Block leaves = config.foliageProvider.getState(level, random, origin).getBlock();
        int height = clearHeight(level, origin, treeHeight, leaves);
        if (height == INVALID_HEIGHT) {
            return attachments;
        }
        int trunkTop = Math.min((int) (height * shape.trunkShare()), height - 1);
        int width = shape.trunkWidth();
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                placeBelowTrunkBlock(level, trunkSetter, random, origin.offset(dx, -1, dz), config);
            }
        }
        List<CrownNode> lower = survey(level, random, origin, shape.crownWidth(), height, trunkTop, leaves);
        placeColumn(level, trunkSetter, random, config, origin, 0, trunkTop);
        placeLimbs(level, trunkSetter, random, config, origin, height, lower, attachments);
        if (shape.stackedCrown()) {
            BlockPos upper = origin.above(trunkTop);
            List<CrownNode> stacked = survey(level, random, upper, STACKED_CROWN_WIDTH, height, trunkTop, leaves);
            placeColumn(level, trunkSetter, random, config, upper, 1, trunkTop);
            placeLimbs(level, trunkSetter, random, config, upper, height, stacked, attachments);
        }
        return attachments;
    }

    private int clearHeight(WorldGenLevel level, BlockPos origin, int treeHeight, Block leaves) {
        int limit = treeHeight;
        int width = shape.trunkWidth();
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                BlockState ground = level.getBlockState(origin.offset(dx, -1, dz));
                if (!ground.is(BlockTags.SUBSTRATE_OVERWORLD) && !ground.is(Blocks.FARMLAND)) {
                    return INVALID_HEIGHT;
                }
                for (int y = 0; y < limit; y++) {
                    BlockPos pos = origin.offset(dx, y, dz);
                    if (!level.getFluidState(pos).isEmpty()) {
                        return INVALID_HEIGHT;
                    }
                    if (!isOpen(level, pos, leaves)) {
                        limit = y;
                        break;
                    }
                }
                if (limit < MIN_TREE_HEIGHT) {
                    return INVALID_HEIGHT;
                }
            }
        }
        return limit;
    }

    private List<CrownNode> survey(WorldGenLevel level, RandomSource random, BlockPos base, double crownWidth, int height, int trunkTop, Block leaves) {
        List<CrownNode> nodes = new ArrayList<>();
        int highestStart = base.getY() + trunkTop;
        nodes.add(new CrownNode(base.above(height - TOP_CLUSTER_DROP), highestStart));
        int crownFloor = (int) (height * CROWN_FLOOR_FRACTION);
        int perLayer = Math.max(1, (int) (LAYER_BASE + Math.pow(LAYER_HEIGHT_FACTOR * height / LAYER_HEIGHT_DIVISOR, 2.0)));
        for (int layer = height - TOP_CLUSTER_DROP; !rule.belowCrown(layer, crownFloor); layer--) {
            double reach = crownWidth * layerShrink(height, layer);
            for (int attempt = 0; attempt < perLayer; attempt++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double distance = reach * (DISTANCE_MIN + random.nextDouble());
                int x = rule.clusterCoordinate(base.getX(), distance * Math.sin(angle));
                int z = rule.clusterCoordinate(base.getZ(), distance * Math.cos(angle));
                BlockPos cluster = new BlockPos(x, base.getY() + layer - 1, z);
                double horizontal = Math.hypot(x - base.getX(), z - base.getZ());
                int limbStart = Math.min((int) (cluster.getY() - horizontal * shape.branchSlope()), highestStart);
                BlockPos trunkPoint = new BlockPos(base.getX(), limbStart, base.getZ());
                if (isClear(level, cluster, cluster.above(CLUSTER_CLEARANCE), leaves) && isClear(level, trunkPoint, cluster, leaves)) {
                    nodes.add(new CrownNode(cluster, limbStart));
                }
            }
        }
        return nodes;
    }

    private static double layerShrink(int height, int layer) {
        double half = height / 2.0;
        double offset = half - layer;
        double span = half * half - offset * offset;
        return span <= 0.0 ? 0.0 : SHRINK_FACTOR * Math.sqrt(span);
    }

    private void placeLimbs(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos base, int height, List<CrownNode> nodes, List<FoliagePlacer.FoliageAttachment> attachments) {
        for (CrownNode node : nodes) {
            if (node.branchFootY() - base.getY() < LIMB_FLOOR_FRACTION * height) {
                continue;
            }
            BlockPos start = new BlockPos(base.getX(), node.branchFootY(), base.getZ());
            BlockPos cluster = node.cluster();
            Direction.Axis axis = limbAxis(start, cluster);
            List<BlockPos> run = run(start, cluster);
            for (int index = 1; index < run.size(); index++) {
                placeLog(level, trunkSetter, random, config, run.get(index), axis);
            }
            attachments.add(new FoliagePlacer.FoliageAttachment(cluster, 0, false));
        }
    }

    private void placeColumn(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos base, int fromOffset, int toOffset) {
        int width = shape.trunkWidth();
        for (int y = fromOffset; y <= toOffset; y++) {
            for (int dx = 0; dx < width; dx++) {
                for (int dz = 0; dz < width; dz++) {
                    placeLog(level, trunkSetter, random, config, base.offset(dx, y, dz), Direction.Axis.Y);
                }
            }
        }
    }

    private void placeLog(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos pos, Direction.Axis axis) {
        if (level.isOutsideBuildHeight(pos) || !CrownRule.canHostLog(level.getBlockState(pos))) {
            return;
        }
        BlockState log = config.trunkProvider.getState(level, random, pos);
        if (log.hasProperty(RotatedPillarBlock.AXIS)) {
            log = log.setValue(RotatedPillarBlock.AXIS, axis);
        }
        trunkSetter.accept(pos, log);
    }

    private static Direction.Axis limbAxis(BlockPos start, BlockPos end) {
        int dx = Math.abs(end.getX() - start.getX());
        int dz = Math.abs(end.getZ() - start.getZ());
        if (dx == 0 && dz == 0) {
            return Direction.Axis.Y;
        }
        return dx >= dz ? Direction.Axis.X : Direction.Axis.Z;
    }

    private List<BlockPos> run(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int span = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        List<BlockPos> cells = new ArrayList<>();
        if (span == 0) {
            return cells;
        }
        for (int step = 0; step <= span; step++) {
            cells.add(new BlockPos(rule.lineCoordinate(from.getX(), dx, step, span, Math.abs(dx) == span), rule.lineCoordinate(from.getY(), dy, step, span, Math.abs(dy) == span),
                    rule.lineCoordinate(from.getZ(), dz, step, span, Math.abs(dz) == span)));
        }
        return cells;
    }

    private boolean isClear(WorldGenLevel level, BlockPos from, BlockPos to, Block leaves) {
        for (BlockPos cell : run(from, to)) {
            if (!isOpen(level, cell, leaves)) {
                return false;
            }
        }
        return true;
    }

    private boolean isOpen(WorldGenLevel level, BlockPos pos, Block leaves) {
        return !level.isOutsideBuildHeight(pos) && rule.isOpen(level.getBlockState(pos), leaves);
    }
}
