package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.leclowndu93150.thaumaturge.content.world.tree.TrunkCorner;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import org.jspecify.annotations.Nullable;

public final class CrownTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<CrownTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance).and(CrownShape.CODEC.fieldOf("shape").forGetter(placer -> placer.shape))
            .and(CrownRule.CODEC.fieldOf("rule").forGetter(placer -> placer.rule)).apply(instance, CrownTrunkPlacer::new));

    private static final int MIN_HEIGHT_LIMIT = 6;
    private static final int CLUSTER_LAYERS = 4;
    private static final int CLUSTER_PROBE_LENGTH = 5;
    private static final int CLUSTER_LAYER_DROP = 1;
    private static final double CROWN_FLOOR_SHARE = 0.3;
    private static final double BRANCH_FLOOR_SHARE = 0.2;
    private static final double CLUSTER_COUNT_BASE = 1.382;
    private static final double CLUSTER_COUNT_HEIGHT_SHARE = 0.9;
    private static final double CLUSTER_COUNT_REFERENCE_HEIGHT = 13.0;
    private static final double PROFILE_SCALE = 0.5;
    private static final double REACH_FLOOR = 0.328;
    private static final double UPPER_CROWN_WIDTH = 1.66;

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
    public boolean isFree(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos);
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int treeHeight, BlockPos origin, TreeConfiguration config) {
        Block ownLeaves = config.foliageProvider.getState(level, random, origin).getBlock();
        if (!standsOnSoil(level, origin)) {
            return List.of();
        }
        if (baseHasFluid(level, origin)) {
            return List.of();
        }
        int limit = clearHeight(level, origin, treeHeight, ownLeaves);
        if (limit < MIN_HEIGHT_LIMIT) {
            return List.of();
        }
        List<FoliagePlacer.FoliageAttachment> attachments = new ArrayList<>();
        Crown lower = new Crown(origin, limit, shape.crownWidth(), ownLeaves);
        int lowerTop = lower.grow(level, trunkSetter, random, config, attachments);
        if (shape.stackedCrown()) {
            Crown upper = new Crown(origin.atY(lowerTop), limit, UPPER_CROWN_WIDTH, ownLeaves);
            upper.grow(level, trunkSetter, random, config, attachments);
        }
        return attachments;
    }

    private boolean standsOnSoil(WorldGenLevel level, BlockPos origin) {
        int width = shape.trunkWidth();
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                if (!level.isStateAtPosition(origin.offset(dx, -1, dz), state -> state.is(BlockTags.SUPPORTS_VEGETATION))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean baseHasFluid(WorldGenLevel level, BlockPos origin) {
        int width = shape.trunkWidth();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < width; dz++) {
                cursor.setWithOffset(origin, dx, 0, dz);
                if (!level.getFluidState(cursor).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int clearHeight(WorldGenLevel level, BlockPos origin, int limit, Block ownLeaves) {
        int width = shape.trunkWidth();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < limit; dy++) {
            for (int dx = 0; dx < width; dx++) {
                for (int dz = 0; dz < width; dz++) {
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (!rule.isOpen(level.getBlockState(cursor), ownLeaves)) {
                        return dy;
                    }
                }
            }
        }
        return limit;
    }

    private static int clustersPerLayer(int limit) {
        double scaled = CLUSTER_COUNT_HEIGHT_SHARE * limit / CLUSTER_COUNT_REFERENCE_HEIGHT;
        return Math.max(1, Mth.floor(CLUSTER_COUNT_BASE + scaled * scaled));
    }

    private static double profile(int limit, int layer) {
        double half = limit / 2.0;
        double fromMiddle = half - layer;
        if (Math.abs(fromMiddle) >= half) {
            return 0.0;
        }
        return PROFILE_SCALE * Math.sqrt(half * half - fromMiddle * fromMiddle);
    }

    private static Direction.Axis branchAxis(int offsetX, int offsetZ) {
        int absX = Math.abs(offsetX);
        int absZ = Math.abs(offsetZ);
        if (absX == 0 && absZ == 0) {
            return Direction.Axis.Y;
        }
        return absX >= absZ ? Direction.Axis.X : Direction.Axis.Z;
    }

    private static int span(int dx, int dy, int dz) {
        return Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
    }

    private final class Crown {
        private final BlockPos base;
        private final int limit;
        private final double width;
        private final Block ownLeaves;
        private final int trunkTop;

        private Crown(BlockPos base, int limit, double width, Block ownLeaves) {
            this.base = base;
            this.limit = limit;
            this.width = width;
            this.ownLeaves = ownLeaves;
            this.trunkTop = base.getY() + Mth.floor(limit * shape.trunkShare());
        }

        private int grow(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, List<FoliagePlacer.FoliageAttachment> attachments) {
            List<CrownNode> nodes = new ArrayList<>();
            BlockPos summit = new BlockPos(base.getX(), base.getY() + limit - CLUSTER_LAYERS, base.getZ());
            nodes.add(new CrownNode(summit, trunkTop));
            int perLayer = clustersPerLayer(limit);
            int crownFloor = Mth.ceil(limit * CROWN_FLOOR_SHARE);
            for (int layer = limit - CLUSTER_LAYERS; !rule.belowCrown(layer, crownFloor); layer--) {
                double reach = width * profile(limit, layer);
                for (int attempt = 0; attempt < perLayer; attempt++) {
                    CrownNode node = scout(level, random, layer, reach);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
            }
            raiseTrunk(level, trunkSetter, random, config);
            double branchFloor = base.getY() + limit * BRANCH_FLOOR_SHARE;
            for (CrownNode node : nodes) {
                if (node.branchFootY() >= branchFloor) {
                    growBranch(level, trunkSetter, random, config, node);
                    attachments.add(new FoliagePlacer.FoliageAttachment(node.cluster(), 0, false));
                }
            }
            return trunkTop;
        }

        private @Nullable CrownNode scout(WorldGenLevel level, RandomSource random, int layer, double reach) {
            double distance = reach * (random.nextFloat() + REACH_FLOOR);
            double angle = random.nextFloat() * Mth.TWO_PI;
            int x = rule.clusterCoordinate(base.getX(), distance * Math.sin(angle));
            int z = rule.clusterCoordinate(base.getZ(), distance * Math.cos(angle));
            BlockPos cluster = new BlockPos(x, base.getY() + layer - CLUSTER_LAYER_DROP, z);
            if (!clusterHasRoom(level, cluster)) {
                return null;
            }
            int runX = cluster.getX() - base.getX();
            int runZ = cluster.getZ() - base.getZ();
            double run = Math.sqrt(runX * runX + runZ * runZ);
            int footY = Math.min(Mth.floor(cluster.getY() - run * shape.branchSlope()), trunkTop);
            if (!lineIsOpen(level, new BlockPos(base.getX(), footY, base.getZ()), cluster)) {
                return null;
            }
            return new CrownNode(cluster, footY);
        }

        private boolean clusterHasRoom(WorldGenLevel level, BlockPos cluster) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dy = 0; dy < CLUSTER_PROBE_LENGTH; dy++) {
                cursor.setWithOffset(cluster, 0, dy, 0);
                if (!isPassable(level, cursor)) {
                    return false;
                }
            }
            return true;
        }

        private boolean lineIsOpen(WorldGenLevel level, BlockPos start, BlockPos end) {
            int dx = end.getX() - start.getX();
            int dy = end.getY() - start.getY();
            int dz = end.getZ() - start.getZ();
            int steps = span(dx, dy, dz);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int step = 0; step <= steps; step++) {
                pointOnLine(start, dx, dy, dz, step, steps, cursor);
                if (!isPassable(level, cursor)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isPassable(WorldGenLevel level, BlockPos pos) {
            if (level.isOutsideBuildHeight(pos)) {
                return false;
            }
            BlockState state = level.getBlockState(pos);
            return rule.isOpen(state, ownLeaves);
        }

        private void raiseTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config) {
            int width = shape.trunkWidth();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int y = base.getY(); y <= trunkTop; y++) {
                for (int dx = 0; dx < width; dx++) {
                    for (int dz = 0; dz < width; dz++) {
                        cursor.set(base.getX() + dx, y, base.getZ() + dz);
                        setLog(level, trunkSetter, random, config, cursor, Direction.Axis.Y, TrunkCorner.atOffset(dx, dz, width));
                    }
                }
            }
        }

        private void growBranch(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, CrownNode node) {
            BlockPos end = node.cluster();
            BlockPos start = new BlockPos(base.getX(), node.branchFootY(), base.getZ());
            int dx = end.getX() - start.getX();
            int dy = end.getY() - start.getY();
            int dz = end.getZ() - start.getZ();
            int steps = span(dx, dy, dz);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int step = 1; step <= steps; step++) {
                pointOnLine(start, dx, dy, dz, step, steps, cursor);
                if (isTrunkCell(cursor)) {
                    continue;
                }
                setLog(level, trunkSetter, random, config, cursor, branchAxis(cursor.getX() - start.getX(), cursor.getZ() - start.getZ()));
            }
        }

        private boolean isTrunkCell(BlockPos pos) {
            int last = shape.trunkWidth() - 1;
            return pos.getY() >= base.getY() && pos.getY() <= trunkTop && pos.getX() >= base.getX() && pos.getX() <= base.getX() + last && pos.getZ() >= base.getZ()
                    && pos.getZ() <= base.getZ() + last;
        }

        private void pointOnLine(BlockPos start, int dx, int dy, int dz, int step, int steps, BlockPos.MutableBlockPos cursor) {
            if (steps == 0) {
                cursor.set(start);
                return;
            }
            int x = rule.lineCoordinate(start.getX(), dx, step, steps, Math.abs(dx) == steps);
            int y = rule.lineCoordinate(start.getY(), dy, step, steps, Math.abs(dy) == steps);
            int z = rule.lineCoordinate(start.getZ(), dz, step, steps, Math.abs(dz) == steps);
            cursor.set(x, y, z);
        }

        private void setLog(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos pos, Direction.Axis axis) {
            setLog(level, trunkSetter, random, config, pos, axis, TrunkCorner.NONE);
        }

        private void setLog(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, TreeConfiguration config, BlockPos pos, Direction.Axis axis, TrunkCorner corner) {
            if (level.isOutsideBuildHeight(pos) || !level.isStateAtPosition(pos, CrownRule::canHostLog)) {
                return;
            }
            trunkSetter.accept(pos, config.trunkProvider.getState(level, random, pos).trySetValue(RotatedPillarBlock.AXIS, axis).trySetValue(TrunkCorner.PROPERTY, corner));
        }
    }
}
