package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class SilverwoodTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<SilverwoodTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance)
            .and(instance.group(Codec.BOOL.fieldOf("grow_nodes").forGetter(placer -> placer.growNodes), Codec.BOOL.fieldOf("keep_apart").forGetter(placer -> placer.keepApart)))
            .apply(instance, SilverwoodTrunkPlacer::new));

    private static final int LEAF_WRITE_FLAGS = Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE;
    private static final int NARROW_HALF_WIDTH = 1;
    private static final int WIDE_HALF_WIDTH = 3;
    private static final int WIDE_LAYERS_BELOW_TOP = 1;
    private static final int CLEARANCE_ABOVE_TOP = 1;
    private static final int SIBLING_RADIUS = 12;
    private static final int SIBLING_DEPTH_BELOW = 8;
    private static final int SIBLING_HEIGHT_ABOVE = 8;
    private static final int CANOPY_RADIUS = 5;
    private static final int CANOPY_DEPTH_BELOW = 1;
    private static final int CANOPY_HEIGHT_ABOVE = 5;
    private static final int CANOPY_FLOOR_BELOW_TOP = 5;
    private static final int CANOPY_TRUNK_REACH_SQUARED = 4;
    private static final int CROWN_RADIUS = 5;
    private static final int CROWN_DEPTH_BELOW_TOP = 5;
    private static final int CROWN_CORE_DEPTH = 3;
    private static final int CROWN_EXTRA_HEIGHT_BASE = 3;
    private static final int CROWN_EXTRA_HEIGHT_RANGE = 3;
    private static final int CROWN_THRESHOLD_BASE = 10;
    private static final int CROWN_THRESHOLD_RANGE = 8;
    private static final float NODE_ODDS_PER_HEIGHT = 1.5F;
    private static final int FLARE_RAISED_CHANCE_NUMERATOR = 2;
    private static final int FLARE_RAISED_CHANCE_DENOMINATOR = 3;
    private static final int BOUGH_LOWER_CHANCE_DENOMINATOR = 3;
    private static final int BOUGH_FORK_DEPTH_BELOW_TOP = 4;
    private static final int ARM_LENGTH = 2;
    private static final int LEAF_ATTACHMENT_RADIUS = 0;

    private final boolean growNodes;
    private final boolean keepApart;

    public SilverwoodTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, boolean growNodes, boolean keepApart) {
        super(baseHeight, heightRandA, heightRandB);
        this.growNodes = growNodes;
        this.keepApart = keepApart;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TTTreePlacers.SILVERWOOD_TRUNK.get();
    }

    @Override
    public int getTreeHeight(RandomSource random) {
        int height = this.baseHeight + random.nextInt(this.heightRandA + 1);
        if (this.heightRandB > 0) {
            height += random.nextInt(this.heightRandB + 1);
        }
        return height;
    }

    @Override
    public boolean isFree(WorldGenLevel level, BlockPos pos) {
        return true;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int height, BlockPos origin, TreeConfiguration config) {
        if (!siteAccepts(level, random, height, origin, config)) {
            return List.of();
        }
        BlockState leaf = config.foliageProvider.getState(level, random, origin);
        List<FoliagePlacer.FoliageAttachment> cells = sowCrown(level, random, height, origin, leaf);
        LogSink sink = new LogSink(level, trunkSetter, random, config);
        raiseTrunk(sink, level, random, height, origin);
        placeFlare(sink, random, origin);
        placeBoughs(sink, random, origin.above(height - BOUGH_FORK_DEPTH_BELOW_TOP));
        return cells;
    }

    private boolean siteAccepts(WorldGenLevel level, RandomSource random, int height, BlockPos origin, TreeConfiguration config) {
        boolean basicsPass = origin.getY() + height + CLEARANCE_ABOVE_TOP <= level.getMaxY() && hasClearance(level, height, origin) && level.getFluidState(origin).isEmpty()
                && isPlantableSoil(level.getBlockState(origin.below()));
        if (!basicsPass) {
            return false;
        }
        if (this.keepApart) {
            return !hasSiblingTrunk(level, height, origin, trunkBlockOf(level, random, origin, config)) && !hasCrowdedCanopy(level, height, origin);
        }
        return true;
    }

    private static Block trunkBlockOf(WorldGenLevel level, RandomSource random, BlockPos origin, TreeConfiguration config) {
        return config.trunkProvider.getState(level, random, origin).getBlock();
    }

    private static boolean isPlantableSoil(BlockState soil) {
        return soil.is(BlockTags.SUBSTRATE_OVERWORLD) || soil.is(Blocks.FARMLAND);
    }

    private static boolean hasClearance(WorldGenLevel level, int height, BlockPos origin) {
        int baseY = origin.getY();
        boolean clear = !level.isOutsideBuildHeight(baseY);
        int rise = 1;
        while (clear && rise <= height + CLEARANCE_ABOVE_TOP) {
            clear = !level.isOutsideBuildHeight(baseY + rise) && layerHoldable(level, origin, baseY + rise, halfWidthAt(rise, height));
            rise++;
        }
        return clear;
    }

    private static int halfWidthAt(int rise, int height) {
        return rise >= height - WIDE_LAYERS_BELOW_TOP ? WIDE_HALF_WIDTH : NARROW_HALF_WIDTH;
    }

    private static boolean layerHoldable(WorldGenLevel level, BlockPos origin, int y, int half) {
        int side = half * 2 + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cell = 0; cell < side * side; cell++) {
            cursor.set(origin.getX() + cell / side - half, y, origin.getZ() + cell % side - half);
            if (!canHold(level.getBlockState(cursor))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasSiblingTrunk(WorldGenLevel level, int height, BlockPos origin, Block trunkBlock) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SIBLING_RADIUS; dx <= SIBLING_RADIUS; dx++) {
            for (int dz = -SIBLING_RADIUS; dz <= SIBLING_RADIUS; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy = -SIBLING_DEPTH_BELOW; dy <= height + SIBLING_HEIGHT_ABOVE; dy++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (level.getBlockState(cursor).is(trunkBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasCrowdedCanopy(WorldGenLevel level, int height, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -CANOPY_RADIUS; dx <= CANOPY_RADIUS; dx++) {
            for (int dz = -CANOPY_RADIUS; dz <= CANOPY_RADIUS; dz++) {
                boolean outsideTrunkReach = dx * dx + dz * dz > CANOPY_TRUNK_REACH_SQUARED;
                for (int dy = -CANOPY_DEPTH_BELOW; dy <= height + CANOPY_HEIGHT_ABOVE; dy++) {
                    if (outsideTrunkReach && dy < height - CANOPY_FLOOR_BELOW_TOP) {
                        continue;
                    }
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<FoliagePlacer.FoliageAttachment> sowCrown(WorldGenLevel level, RandomSource random, int height, BlockPos origin, BlockState leaf) {
        int top = origin.getY() + height;
        int crownTop = top + CROWN_EXTRA_HEIGHT_BASE + random.nextInt(CROWN_EXTRA_HEIGHT_RANGE);
        List<FoliagePlacer.FoliageAttachment> cells = new ArrayList<>();
        int layer = top - CROWN_DEPTH_BELOW_TOP;
        while (layer <= crownTop) {
            sowCrownLayer(level, random, origin, layer, Mth.clamp(layer, top - CROWN_CORE_DEPTH, top), leaf, cells);
            layer++;
        }
        return cells;
    }

    private static void sowCrownLayer(WorldGenLevel level, RandomSource random, BlockPos origin, int y, int coreY, BlockState leaf, List<FoliagePlacer.FoliageAttachment> cells) {
        int gapSquared = (y - coreY) * (y - coreY);
        int side = CROWN_RADIUS * 2 + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cell = 0; cell < side * side; cell++) {
            int dx = cell / side - CROWN_RADIUS;
            int dz = cell % side - CROWN_RADIUS;
            int threshold = CROWN_THRESHOLD_BASE + random.nextInt(CROWN_THRESHOLD_RANGE);
            if (dx * dx + dz * dz + gapSquared < threshold) {
                cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
                if (sowLeaf(level, cursor, leaf)) {
                    cells.add(new FoliagePlacer.FoliageAttachment(cursor.immutable(), LEAF_ATTACHMENT_RADIUS, false));
                }
            }
        }
    }

    private static boolean sowLeaf(WorldGenLevel level, BlockPos pos, BlockState leaf) {
        BlockState existing = level.getBlockState(pos);
        if (existing.is(leaf.getBlock())) {
            return false;
        }
        if (existing.is(BlockTags.LEAVES)) {
            level.setBlock(pos, TreeLeafUpdater.carryDistance(leaf, existing), LEAF_WRITE_FLAGS);
            return true;
        }
        if (existing.isAir() || existing.canBeReplaced()) {
            level.setBlock(pos, leaf, LEAF_WRITE_FLAGS);
            return true;
        }
        return false;
    }

    private void raiseTrunk(LogSink sink, WorldGenLevel level, RandomSource random, int height, BlockPos origin) {
        NodeRoller roller = new NodeRoller(this.growNodes, height);
        int rise = -1;
        while (++rise < height) {
            BlockPos centre = origin.above(rise);
            if (canHold(level.getBlockState(centre))) {
                boolean node = roller.roll(rise, random);
                if (node) {
                    placeNodeLog(sink, level, random, centre);
                }
                placeShaft(sink, centre, !node);
            }
        }
        sink.place(origin.above(height), Direction.Axis.Y);
    }

    private static void placeShaft(LogSink sink, BlockPos centre, boolean includeCentre) {
        if (includeCentre) {
            sink.place(centre, Direction.Axis.Y);
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            sink.place(centre.relative(side), Direction.Axis.Y);
        }
    }

    private static void placeNodeLog(LogSink sink, WorldGenLevel level, RandomSource random, BlockPos centre) {
        BlockState nodeLog = TTBlocks.SILVERWOOD_NODE_LOG.get().defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
        sink.setter().accept(centre, nodeLog);
        NodeGenerator.createRandomNodeAt(level, centre, random, true, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
    }

    private static void placeFlare(LogSink sink, RandomSource random, BlockPos origin) {
        for (Direction first : Direction.Plane.HORIZONTAL) {
            BlockPos diagonal = origin.relative(first).relative(first.getClockWise());
            sink.place(diagonal, Direction.Axis.Y);
            if (random.nextInt(FLARE_RAISED_CHANCE_DENOMINATOR) < FLARE_RAISED_CHANCE_NUMERATOR) {
                sink.place(diagonal.above(), Direction.Axis.Y);
            }
            BlockPos arm = origin.relative(first, ARM_LENGTH);
            sink.place(arm, first.getAxis());
            sink.place(arm.below(), Direction.Axis.Y);
        }
    }

    private static void placeBoughs(LogSink sink, RandomSource random, BlockPos fork) {
        for (Direction first : Direction.Plane.HORIZONTAL) {
            BlockPos diagonal = fork.relative(first).relative(first.getClockWise());
            sink.place(diagonal, Direction.Axis.Y);
            if (random.nextInt(BOUGH_LOWER_CHANCE_DENOMINATOR) == 0) {
                sink.place(diagonal.below(), Direction.Axis.Y);
            }
            sink.place(fork.relative(first, ARM_LENGTH), first.getAxis());
        }
    }

    private static boolean canHold(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }

    private static final class NodeRoller {
        private final boolean enabled;
        private final int height;
        private int odds;
        private boolean cooling;

        private NodeRoller(boolean enabled, int height) {
            this.enabled = enabled;
            this.height = height;
            this.odds = Math.max(1, (int) (height * NODE_ODDS_PER_HEIGHT));
        }

        private boolean roll(int rise, RandomSource random) {
            boolean hit = this.enabled && rise >= 1 && !this.cooling && random.nextInt(this.odds) == 0;
            if (hit) {
                this.odds += this.height;
            }
            this.cooling = hit;
            return hit;
        }
    }

    private record LogSink(WorldGenLevel level, BiConsumer<BlockPos, BlockState> setter, RandomSource random, TreeConfiguration config) {
        void place(BlockPos pos, Direction.Axis axis) {
            if (!canHold(this.level.getBlockState(pos))) {
                return;
            }
            BlockState log = this.config.trunkProvider.getState(this.level, this.random, pos);
            if (log.hasProperty(BlockStateProperties.AXIS)) {
                log = log.setValue(BlockStateProperties.AXIS, axis);
            }
            this.setter.accept(pos.immutable(), log);
        }
    }
}
