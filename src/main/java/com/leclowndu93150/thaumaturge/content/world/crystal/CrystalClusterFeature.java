package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig.Entry;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.neoforged.neoforge.common.Tags;

public final class CrystalClusterFeature extends Feature<CrystalClusterConfig> {
    private static final int CLUSTER_SPREAD = 6;
    private static final int CLUSTER_OFFSET = 8;
    private static final int SURFACE_MARGIN = 5;
    private static final int CELL_REACH = 1;
    private static final int CELL_SKIP_ONE_IN = 3;
    private static final int SIZE_VARIATION = 3;
    private static final int PLACE_FLAGS = 19;
    private static final int OVERSHOOT_ALLOWANCE = 1;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final List<Vec3i> RADIAL_OFFSETS = buildRadialOffsets();

    public CrystalClusterFeature(Codec<CrystalClusterConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalClusterConfig> context) {
        CrystalClusterConfig config = context.config();
        if (config.crystals().isEmpty()) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        CenterStrategy centers = config.atOrigin() ? new OriginCenter() : new UndergroundCenter();
        CrystalSelector selector = new BiomeFlavouredSelector(config.crystals(), config.biomeAspectChance());
        PlacementBudget budget = new PlacementBudget(config.attempts(), config.maxTotal());
        boolean placed = false;
        while (budget.canStart()) {
            budget.consumeAttempt();
            BlockPos center = centers.pick(level, random, context.origin());
            if (center == null) {
                continue;
            }
            Entry entry = selector.pick(random, level.getBiome(center));
            BlockState base = entry.block().defaultBlockState();
            if (!base.hasProperty(BlockCrystal.SIZE)) {
                continue;
            }
            List<BlockPos> cells = planCells(level, random, center);
            int added = placeCells(level, random, base, cells);
            budget.spend(added);
            placed |= added > 0;
        }
        return placed;
    }

    private static List<Vec3i> buildRadialOffsets() {
        List<Vec3i> offsets = new ArrayList<>();
        for (int dx = -CELL_REACH; dx <= CELL_REACH; dx++) {
            for (int dy = -CELL_REACH; dy <= CELL_REACH; dy++) {
                for (int dz = -CELL_REACH; dz <= CELL_REACH; dz++) {
                    offsets.add(new Vec3i(dx, dy, dz));
                }
            }
        }
        offsets.sort(Comparator.comparingDouble(offset -> offset.distSqr(Vec3i.ZERO)));
        return List.copyOf(offsets);
    }

    private static List<BlockPos> planCells(WorldGenLevel level, RandomSource random, BlockPos center) {
        List<BlockPos> candidates = new ArrayList<>(RADIAL_OFFSETS.size());
        for (Vec3i offset : RADIAL_OFFSETS) {
            candidates.add(center.offset(offset));
        }
        candidates.removeIf(pos -> !canHold(level, pos));
        candidates.removeIf(pos -> random.nextInt(CELL_SKIP_ONE_IN) == 0);
        return candidates;
    }

    private static int placeCells(WorldGenLevel level, RandomSource random, BlockState base, List<BlockPos> cells) {
        int total = 0;
        for (BlockPos pos : cells) {
            int size = 1 + random.nextInt(SIZE_VARIATION);
            level.setBlock(pos, base.setValue(BlockCrystal.SIZE, size), PLACE_FLAGS);
            total += size;
        }
        return total;
    }

    private static boolean canHold(WorldGenLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.getFluidState().isEmpty() || !(state.isAir() || state.canBeReplaced())) {
            return false;
        }
        for (Direction direction : DIRECTIONS) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (neighbour.is(BlockTags.BASE_STONE_OVERWORLD) || neighbour.is(Tags.Blocks.STONES) || neighbour.is(Tags.Blocks.ORES) || neighbour.is(BlockTags.BASE_STONE_NETHER)) {
                return true;
            }
        }
        return false;
    }

    private static final class PlacementBudget {
        private int attemptsLeft;
        private int sizeLeft;

        private PlacementBudget(int attempts, int maxTotal) {
            this.attemptsLeft = attempts;
            this.sizeLeft = maxTotal + OVERSHOOT_ALLOWANCE;
        }

        private boolean canStart() {
            return attemptsLeft > 0 && sizeLeft > 0;
        }

        private void consumeAttempt() {
            attemptsLeft--;
        }

        private void spend(int size) {
            sizeLeft -= size;
        }
    }

    private interface CenterStrategy {
        BlockPos pick(WorldGenLevel level, RandomSource random, BlockPos origin);
    }

    private static final class OriginCenter implements CenterStrategy {
        @Override
        public BlockPos pick(WorldGenLevel level, RandomSource random, BlockPos origin) {
            return origin;
        }
    }

    private static final class UndergroundCenter implements CenterStrategy {
        @Override
        public BlockPos pick(WorldGenLevel level, RandomSource random, BlockPos origin) {
            int z = origin.getZ() + CLUSTER_OFFSET + random.nextInt(2 * CLUSTER_SPREAD + 1) - CLUSTER_SPREAD;
            int x = origin.getX() + CLUSTER_OFFSET + random.nextInt(2 * CLUSTER_SPREAD + 1) - CLUSTER_SPREAD;
            int floor = level.getMinY();
            int ceiling = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - SURFACE_MARGIN;
            if (ceiling < floor) {
                return null;
            }
            return new BlockPos(x, floor + random.nextInt(ceiling - floor + 1), z);
        }
    }

    private interface CrystalSelector {
        Entry pick(RandomSource random, Holder<Biome> biome);
    }

    private static final class BiomeFlavouredSelector implements CrystalSelector {
        private final List<Entry> crystals;
        private final int biomeAspectChance;

        private BiomeFlavouredSelector(List<Entry> crystals, int biomeAspectChance) {
            this.crystals = crystals;
            this.biomeAspectChance = biomeAspectChance;
        }

        @Override
        public Entry pick(RandomSource random, Holder<Biome> biome) {
            if (random.nextInt(biomeAspectChance) != 0 || (biome.is(TTBiomes.TAINTED_LANDS) && random.nextBoolean())) {
                return uniform(random);
            }
            BiomeAspects aspects = biome.getData(TTDataMaps.BIOME_ASPECTS);
            if (aspects == null || aspects.aspects().isEmpty()) {
                return uniform(random);
            }
            ResourceKey<IAspect> wanted = aspects.aspects().get(random.nextInt(aspects.aspects().size()));
            for (Entry entry : crystals) {
                if (entry.aspect().equals(wanted)) {
                    return entry;
                }
            }
            return uniform(random);
        }

        private Entry uniform(RandomSource random) {
            return crystals.get(random.nextInt(crystals.size()));
        }
    }
}
