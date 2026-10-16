package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jspecify.annotations.Nullable;

public final class MagicForestFloraFeature extends Feature<MagicForestFloraConfig> {
    private static final int CHUNK_SPAN = 16;
    private static final int CORE_INSET = 4;
    private static final int CORE_SPAN = 8;
    private static final int GROUND_FLOOR = 30;
    private static final int VISHROOM_FLOOR = 50;
    private static final int MUSHROOM_GRID = 4;
    private static final int MUSHROOM_ORIGIN = 3;
    private static final int MUSHROOM_STEP = 3;
    private static final int LOG_REACH = 1;
    private static final double FLOWER_NOISE_SCALE = 48.0;
    private static final double FLOWER_NOISE_CLAMP_MAX = 0.9999;
    private static final int PLANT_FLAGS = Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE;

    public MagicForestFloraFeature(Codec<MagicForestFloraConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MagicForestFloraConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkGenerator generator = context.chunkGenerator();
        MagicForestFloraConfig config = context.config();
        ChunkPos chunk = ChunkPos.containing(context.origin());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        boolean placed = placeHugeMushrooms(level, generator, random, config, minX, minZ);
        placed |= placeFlowers(level, random, config, minX, minZ);
        placed |= placeGroundCover(level, random, config, minX, minZ);
        placed |= placeAmbientGrass(level, random, config, minX, minZ);
        placed |= placeVishrooms(level, random, config, minX, minZ);
        return placed;
    }

    private static boolean placeAmbientGrass(WorldGenLevel level, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        for (int attempt = 0; attempt < config.grassAttempts(); attempt++) {
            int x = minX + CORE_INSET + random.nextInt(CORE_SPAN);
            int z = minZ + CORE_INSET + random.nextInt(CORE_SPAN);
            BlockPos grass = grassBelowSky(level, x, z, GROUND_FLOOR);
            if (grass != null) {
                level.setBlock(grass, config.ambientGrass().withPropertiesOf(level.getBlockState(grass)), PLANT_FLAGS);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean placeHugeMushrooms(WorldGenLevel level, ChunkGenerator generator, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        for (int cellX = 0; cellX < MUSHROOM_GRID; cellX++) {
            for (int cellZ = 0; cellZ < MUSHROOM_GRID; cellZ++) {
                if (random.nextInt(config.hugeMushroomRarity()) != 0) {
                    continue;
                }
                BlockPos grass = grassBelowSky(level, minX + MUSHROOM_ORIGIN + MUSHROOM_STEP * cellX, minZ + MUSHROOM_ORIGIN + MUSHROOM_STEP * cellZ, GROUND_FLOOR);
                if (grass == null) {
                    continue;
                }
                BlockPos ground = grass.above();
                placed |= config.hugeMushrooms().getRandomElement(random).map(Holder::value).map(mushroom -> mushroom.place(level, generator, random, ground)).orElse(false);
            }
        }
        return placed;
    }

    private static boolean placeFlowers(WorldGenLevel level, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        List<BlockState> flowers = config.flowers().stream().map(holder -> holder.value().defaultBlockState()).toList();
        if (flowers.isEmpty()) {
            return false;
        }
        boolean placed = false;
        for (int attempt = 0; attempt < config.flowerAttempts(); attempt++) {
            BlockPos grass = grassBelowSky(level, minX + random.nextInt(CHUNK_SPAN), minZ + random.nextInt(CHUNK_SPAN), GROUND_FLOOR);
            if (grass != null) {
                BlockPos spot = grass.above();
                placed |= placePlant(level, spot, pickFlower(flowers, spot));
            }
        }
        return placed;
    }

    private static BlockState pickFlower(List<BlockState> flowers, BlockPos spot) {
        double noise = Biome.BIOME_INFO_NOISE.getValue(spot.getX() / FLOWER_NOISE_SCALE, spot.getZ() / FLOWER_NOISE_SCALE, false);
        double scaled = Mth.clamp((1.0 + noise) / 2.0, 0.0, FLOWER_NOISE_CLAMP_MAX);
        return flowers.get((int) (scaled * flowers.size()));
    }

    private static boolean placeGroundCover(WorldGenLevel level, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        for (MagicForestFloraConfig.PlantPatch patch : config.plants()) {
            for (int attempt = 0; attempt < patch.attempts(); attempt++) {
                if (random.nextInt(patch.rarity()) != 0) {
                    continue;
                }
                BlockPos grass = grassBelowSky(level, minX + random.nextInt(CHUNK_SPAN), minZ + random.nextInt(CHUNK_SPAN), GROUND_FLOOR);
                if (grass != null) {
                    BlockPos spot = grass.above();
                    placed |= placePlant(level, spot, patch.state().getState(level, random, spot));
                }
            }
        }
        return placed;
    }

    private static boolean placePlant(WorldGenLevel level, BlockPos spot, BlockState plant) {
        BlockState current = level.getBlockState(spot);
        if (!current.getFluidState().isEmpty() || !current.canBeReplaced() || !plant.canSurvive(level, spot)) {
            return false;
        }
        if (plant.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            if (spot.getY() >= level.getMaxY() || !level.getBlockState(spot.above()).canBeReplaced()) {
                return false;
            }
            DoublePlantBlock.placeAt(level, plant, spot, PLANT_FLAGS);
            return true;
        }
        level.setBlock(spot, plant, PLANT_FLAGS);
        return true;
    }

    private static boolean placeVishrooms(WorldGenLevel level, RandomSource random, MagicForestFloraConfig config, int minX, int minZ) {
        boolean placed = false;
        BlockState vishroom = config.vishroom().defaultBlockState();
        for (int attempt = 0; attempt < config.vishroomAttempts(); attempt++) {
            BlockPos grass = grassBelowSky(level, minX + random.nextInt(CHUNK_SPAN), minZ + random.nextInt(CHUNK_SPAN), VISHROOM_FLOOR);
            if (grass == null) {
                continue;
            }
            BlockPos spot = grass.above();
            if (roomForPlant(level.getBlockState(spot)) && besideLog(level, spot) && vishroom.canSurvive(level, spot)) {
                level.setBlock(spot, vishroom, PLANT_FLAGS);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean roomForPlant(BlockState state) {
        return state.canBeReplaced() && state.getFluidState().isEmpty() && !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
    }

    private static boolean besideLog(WorldGenLevel level, BlockPos spot) {
        return BlockPos.betweenClosedStream(spot.offset(-LOG_REACH, -LOG_REACH, -LOG_REACH), spot.offset(LOG_REACH, LOG_REACH, LOG_REACH)).anyMatch(pos -> level.getBlockState(pos).is(BlockTags.LOGS));
    }

    private static @Nullable BlockPos grassBelowSky(WorldGenLevel level, int x, int z, int floorY) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
        while (probe.getY() > floorY) {
            if (level.getBlockState(probe).is(Blocks.GRASS_BLOCK)) {
                return probe.immutable();
            }
            probe.move(0, -1, 0);
        }
        return null;
    }
}
