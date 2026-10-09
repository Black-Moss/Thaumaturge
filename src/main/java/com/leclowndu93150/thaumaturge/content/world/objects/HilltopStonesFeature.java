package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class HilltopStonesFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_HEIGHT = 85;
    private static final int RING_RADIUS = 3;
    private static final int SAMPLE_RADIUS = 2;
    private static final int FILL_DEPTH = 4;
    private static final int NODE_HEIGHT = 5;
    private static final int MAX_SOLID_RUN = 3;
    private static final int PILLAR_CAP = 4;
    private static final int PILLAR_STOP_FROM = 2;
    private static final int PILLAR_RESHAPE_HEIGHT = 4;
    private static final int VINE_ONE_IN = 3;
    private static final int VINE_EXTRA_LENGTH = 4;
    private static final int PLACE_FLAGS = 3;
    private static final int[][] SITE_OFFSETS = {{0, 0}, {SAMPLE_RADIUS, 0}, {-SAMPLE_RADIUS, 0}, {0, SAMPLE_RADIUS}, {0, -SAMPLE_RADIUS}};
    private static final int[][] PILLAR_OFFSETS = {{3, 1}, {3, -1}, {-3, 1}, {-3, -1}, {1, 3}, {-1, 3}, {1, -3}, {-1, -3}};

    public HilltopStonesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        for (int[] offset : SITE_OFFSETS) {
            if (!isValidSpawn(level, origin.getX() + offset[0], origin.getY(), origin.getZ() + offset[1])) {
                return false;
            }
        }
        BlockState fill = findFill(level, origin);
        boolean vines = !level.getBiome(origin).value().coldEnoughToSnow(origin, level.getSeaLevel());
        placeFloor(level, random, origin, fill);
        for (int[] offset : PILLAR_OFFSETS) {
            buildPillar(level, random, origin.offset(offset[0], 0, offset[1]), vines);
        }
        buildCentre(level, random, origin);
        NodeGenerator.createRandomNodeAt(level, origin.above(NODE_HEIGHT), random, false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
        return true;
    }

    private static boolean isValidSpawn(WorldGenLevel level, int x, int startY, int z) {
        if (startY < MIN_HEIGHT) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
        int run = 0;
        while (isSolid(level.getBlockState(cursor))) {
            run++;
            if (run >= MAX_SOLID_RUN) {
                return false;
            }
            cursor.move(Direction.UP);
        }
        if (!level.getBlockState(cursor).isAir()) {
            return false;
        }
        BlockPos ground = cursor.below();
        BlockState groundState = level.getBlockState(ground);
        return isBaseGround(groundState) || (isGroundCover(groundState) && isBaseGround(level.getBlockState(ground.below())));
    }

    private static boolean isSolid(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    private static boolean isBaseGround(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT);
    }

    private static boolean isGroundCover(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS) || state.is(BlockTags.SMALL_FLOWERS) || state.is(TTBlockTags.MAGICAL_PLANTS);
    }

    private static BlockState findFill(WorldGenLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = origin.mutable();
        for (int y = origin.getY() - 1; y >= level.getMinY(); y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            if (isBaseGround(state)) {
                return state.getBlock().defaultBlockState();
            }
            if (!state.isAir() && !isGroundCover(state)) {
                break;
            }
        }
        return Blocks.DIRT.defaultBlockState();
    }

    private static void placeFloor(WorldGenLevel level, RandomSource random, BlockPos origin, BlockState fill) {
        for (int dx = -RING_RADIUS; dx <= RING_RADIUS; dx++) {
            for (int dz = -RING_RADIUS; dz <= RING_RADIUS; dz++) {
                if (Math.abs(dx) == RING_RADIUS && Math.abs(dz) == RING_RADIUS) {
                    continue;
                }
                BlockPos floor = origin.offset(dx, 0, dz);
                BlockState tile = random.nextBoolean() ? TTBlocks.OBSIDIAN_TILE.get().defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
                level.setBlock(floor, tile, PLACE_FLAGS);
                for (int depth = 1; depth <= FILL_DEPTH; depth++) {
                    BlockPos under = floor.below(depth);
                    if (under.getY() < level.getMinY()) {
                        break;
                    }
                    BlockState existing = level.getBlockState(under);
                    if (existing.isAir() || isGroundCover(existing)) {
                        level.setBlock(under, fill, PLACE_FLAGS);
                    }
                }
            }
        }
    }

    private static void buildPillar(WorldGenLevel level, RandomSource random, BlockPos floor, boolean vines) {
        BlockState totem = TTBlocks.OBSIDIAN_TOTEM.get().defaultBlockState();
        int built = 0;
        while (built < PILLAR_CAP) {
            level.setBlock(floor.above(built + 1), totem, PLACE_FLAGS);
            built++;
            if (built >= PILLAR_STOP_FROM && random.nextBoolean()) {
                break;
            }
        }
        ObsidianTotemFeature.reshapeTotems(level, floor, PILLAR_RESHAPE_HEIGHT);
        if (vines) {
            hangVines(level, random, floor.above(built));
        }
    }

    private static void hangVines(WorldGenLevel level, RandomSource random, BlockPos top) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos start = top.relative(side);
            if (!level.getBlockState(start).isAir() || random.nextInt(VINE_ONE_IN) != 0) {
                continue;
            }
            BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(side.getOpposite()), true);
            level.setBlock(start, vine, PLACE_FLAGS);
            for (int step = 1; step <= VINE_EXTRA_LENGTH; step++) {
                BlockPos hanging = start.below(step);
                if (!level.getBlockState(hanging).isAir()) {
                    break;
                }
                level.setBlock(hanging, vine, PLACE_FLAGS);
            }
        }
    }

    private static void buildCentre(WorldGenLevel level, RandomSource random, BlockPos origin) {
        level.setBlock(origin, Blocks.SPAWNER.defaultBlockState(), PLACE_FLAGS);
        BlockEntity spawner = level.getBlockEntity(origin);
        if (spawner instanceof SpawnerBlockEntity spawnerEntity) {
            spawnerEntity.setEntityId(TTEntities.WISP.get(), random);
        }
        level.setBlock(origin.above(), TTBlocks.OBSIDIAN_TILE.get().defaultBlockState(), PLACE_FLAGS);
        BlockPos chestPos = origin.above(2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), PLACE_FLAGS);
        RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, BuiltInLootTables.SIMPLE_DUNGEON);
    }
}
