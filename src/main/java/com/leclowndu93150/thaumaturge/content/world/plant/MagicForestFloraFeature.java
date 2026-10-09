package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig.PlantPatch;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jspecify.annotations.Nullable;

public final class MagicForestFloraFeature extends Feature<MagicForestFloraConfig> {
    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_MASK = -CHUNK_SIZE;
    private static final int MUSHROOM_GRID = 4;
    private static final int MUSHROOM_INSET = 3;
    private static final int MUSHROOM_SPACING = 3;
    private static final int AMBIENT_INSET = 4;
    private static final int AMBIENT_SPAN = 8;
    private static final int GRASS_MIN_HEIGHT = 30;
    private static final int VISHROOM_MIN_HEIGHT = 50;
    private static final double FLOWER_NOISE_SCALE = 48.0;
    private static final double NOISE_MAX_SAMPLE = 0.9999;
    private static final int PLACE_FLAGS = 19;

    public MagicForestFloraFeature(Codec<MagicForestFloraConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MagicForestFloraConfig> context) {
        WorldGenLevel level = context.level();
        MagicForestFloraConfig config = context.config();
        RandomSource random = context.random();
        int chunkX = context.origin().getX() & CHUNK_MASK;
        int chunkZ = context.origin().getZ() & CHUNK_MASK;
        boolean placed = placeHugeMushrooms(context, chunkX, chunkZ);
        placed |= placeFlowers(level, config, random, chunkX, chunkZ);
        placed |= placePatches(level, config, random, chunkX, chunkZ);
        placed |= placeAmbientGrass(level, config, random, chunkX, chunkZ);
        placed |= placeVishrooms(level, config, random, chunkX, chunkZ);
        return placed;
    }

    private static boolean placeHugeMushrooms(FeaturePlaceContext<MagicForestFloraConfig> context, int chunkX, int chunkZ) {
        MagicForestFloraConfig config = context.config();
        HolderSet<ConfiguredFeature<?, ?>> features = config.hugeMushrooms();
        if (features.size() == 0) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        boolean placed = false;
        for (int cell = 0; cell < MUSHROOM_GRID * MUSHROOM_GRID; cell++) {
            if (random.nextInt(config.hugeMushroomRarity()) == 0) {
                int x = chunkX + MUSHROOM_INSET + cell / MUSHROOM_GRID * MUSHROOM_SPACING;
                int z = chunkZ + MUSHROOM_INSET + cell % MUSHROOM_GRID * MUSHROOM_SPACING;
                BlockPos grass = findGrass(level, x, z, GRASS_MIN_HEIGHT);
                if (grass != null) {
                    ConfiguredFeature<?, ?> feature = features.get(random.nextInt(features.size())).value();
                    placed |= feature.place(level, context.chunkGenerator(), random, grass.above());
                }
            }
        }
        return placed;
    }

    private static boolean placeFlowers(WorldGenLevel level, MagicForestFloraConfig config, RandomSource random, int chunkX, int chunkZ) {
        HolderSet<Block> flowers = config.flowers();
        if (flowers.size() == 0) {
            return false;
        }
        boolean placed = false;
        for (int attempt = 0; attempt < config.flowerAttempts(); attempt++) {
            BlockPos grass = findGrass(level, chunkX + random.nextInt(CHUNK_SIZE), chunkZ + random.nextInt(CHUNK_SIZE), GRASS_MIN_HEIGHT);
            if (grass != null) {
                BlockPos cell = grass.above();
                double noise = Biome.BIOME_INFO_NOISE.getValue(cell.getX() / FLOWER_NOISE_SCALE, cell.getZ() / FLOWER_NOISE_SCALE, false);
                double sample = Mth.clamp((1.0 + noise) / 2.0, 0.0, NOISE_MAX_SAMPLE);
                Holder<Block> flower = flowers.get((int) (sample * flowers.size()));
                placed |= plant(level, cell, flower.value().defaultBlockState());
            }
        }
        return placed;
    }

    private static boolean placePatches(WorldGenLevel level, MagicForestFloraConfig config, RandomSource random, int chunkX, int chunkZ) {
        boolean placed = false;
        for (PlantPatch patch : config.plants()) {
            int remaining = patch.attempts();
            while (remaining-- > 0) {
                boolean skipped = patch.rarity() > 1 && random.nextInt(patch.rarity()) != 0;
                BlockPos grass = skipped ? null : findGrass(level, chunkX + random.nextInt(CHUNK_SIZE), chunkZ + random.nextInt(CHUNK_SIZE), GRASS_MIN_HEIGHT);
                if (grass != null) {
                    BlockPos cell = grass.above();
                    placed |= plant(level, cell, patch.state().getState(level, random, cell));
                }
            }
        }
        return placed;
    }

    private static boolean placeAmbientGrass(WorldGenLevel level, MagicForestFloraConfig config, RandomSource random, int chunkX, int chunkZ) {
        boolean placed = false;
        for (int attempt = 0; attempt < config.grassAttempts(); attempt++) {
            BlockPos grass = findGrass(level, chunkX + AMBIENT_INSET + random.nextInt(AMBIENT_SPAN), chunkZ + AMBIENT_INSET + random.nextInt(AMBIENT_SPAN), GRASS_MIN_HEIGHT);
            if (grass != null) {
                level.setBlock(grass, config.ambientGrass().defaultBlockState(), PLACE_FLAGS);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean placeVishrooms(WorldGenLevel level, MagicForestFloraConfig config, RandomSource random, int chunkX, int chunkZ) {
        boolean placed = false;
        for (int attempt = 0; attempt < config.vishroomAttempts(); attempt++) {
            BlockPos grass = findGrass(level, chunkX + random.nextInt(CHUNK_SIZE), chunkZ + random.nextInt(CHUNK_SIZE), VISHROOM_MIN_HEIGHT);
            if (grass != null && touchesLog(level, grass.above())) {
                placed |= plant(level, grass.above(), config.vishroom().defaultBlockState());
            }
        }
        return placed;
    }

    private static @Nullable BlockPos findGrass(WorldGenLevel level, int x, int z, int minHeight) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z); y > minHeight; y--) {
            if (level.getBlockState(cursor.setY(y)).is(Blocks.GRASS_BLOCK)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean touchesLog(WorldGenLevel level, BlockPos cell) {
        for (BlockPos neighbour : BlockPos.betweenClosed(cell.offset(-1, -1, -1), cell.offset(1, 1, 1))) {
            if (!neighbour.equals(cell) && level.getBlockState(neighbour).is(BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean plant(WorldGenLevel level, BlockPos cell, BlockState state) {
        if (state.hasProperty(DoublePlantBlock.HALF)) {
            return plantTall(level, cell, state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        }
        if (!hasRoom(level, cell, state)) {
            return false;
        }
        level.setBlock(cell, state, PLACE_FLAGS);
        return true;
    }

    private static boolean plantTall(WorldGenLevel level, BlockPos cell, BlockState lower) {
        BlockPos upper = cell.above();
        boolean fits = hasRoom(level, cell, lower) && upper.getY() <= level.getMaxY() && level.getBlockState(upper).canBeReplaced();
        if (fits) {
            DoublePlantBlock.placeAt(level, lower, cell, PLACE_FLAGS);
        }
        return fits;
    }

    private static boolean hasRoom(WorldGenLevel level, BlockPos cell, BlockState state) {
        return level.getBlockState(cell).canBeReplaced() && state.canSurvive(level, cell);
    }
}
