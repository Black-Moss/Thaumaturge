package com.leclowndu93150.thaumaturge.content.world.mound;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTStructures;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class MoundPiece extends ScatteredFeaturePiece {
    private static final String PORTAL_KEY = "Portal";
    private static final int INITIAL_Y = 64;
    private static final int GROUND_OFFSET = -10;
    private static final int URN_A_X = 9;
    private static final int URN_A_Y = 2;
    private static final int URN_A_Z = 7;
    private static final int URN_B_X = 9;
    private static final int URN_B_Y = 2;
    private static final int URN_B_Z = 11;
    private static final int CHEST_X = 10;
    private static final int CHEST_Y = 2;
    private static final int CHEST_Z = 9;
    private static final int SKELETON_SPAWNER_X = 4;
    private static final int SKELETON_SPAWNER_Y = 6;
    private static final int SKELETON_SPAWNER_Z = 4;
    private static final int ZOMBIE_SPAWNER_X = 4;
    private static final int ZOMBIE_SPAWNER_Y = 6;
    private static final int ZOMBIE_SPAWNER_Z = 14;
    private static final int PORTAL_X = 9;
    private static final int PORTAL_Y = 2;
    private static final int PORTAL_Z = 9;
    private static final int NODE_X = 9;
    private static final int NODE_Y = 8;
    private static final int NODE_Z = 9;
    private static final int RARE_PERCENT = 10;
    private static final int UNCOMMON_PERCENT = 23;
    private static final int CRATE_PERCENT = 30;
    private static final int PERCENT_ROLL = 100;
    private static final int TRAPPED_CHEST_ONE_IN = 3;
    private static final int TNT_DEPTH = 2;
    private static final int BLOCK_FLAGS = 2;
    private static final int NO_COLUMN_HEIGHT = -1;
    private static final int CHAR_BASE = 'A';

    private boolean portalSpawned;

    public MoundPiece(RandomSource random, int west, int north) {
        super(TTStructures.MOUND_PIECE.get(), west, INITIAL_Y, north, MoundLayout.SIZE_X, MoundLayout.SIZE_Y, MoundLayout.SIZE_Z, Direction.SOUTH);
    }

    public MoundPiece(CompoundTag tag) {
        super(TTStructures.MOUND_PIECE.get(), tag);
        this.portalSpawned = tag.getBooleanOr(PORTAL_KEY, false);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putBoolean(PORTAL_KEY, portalSpawned);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        if (!updateAverageGroundHeight(level, chunkBB, GROUND_OFFSET)) {
            return;
        }
        int[] lowestSolid = stampLayout(level, chunkBB);
        buildFoundation(level, chunkBB, lowestSolid);
        placeLoot(level, random, chunkBB, URN_A_X, URN_A_Y, URN_A_Z);
        placeLoot(level, random, chunkBB, URN_B_X, URN_B_Y, URN_B_Z);
        placeChest(level, random, chunkBB);
        placeSpawner(level, random, chunkBB, SKELETON_SPAWNER_X, SKELETON_SPAWNER_Y, SKELETON_SPAWNER_Z, EntityType.SKELETON);
        placeSpawner(level, random, chunkBB, ZOMBIE_SPAWNER_X, ZOMBIE_SPAWNER_Y, ZOMBIE_SPAWNER_Z, EntityType.ZOMBIE);
        spawnPortal(level, chunkBB);
        placeNode(level, random, chunkBB);
    }

    private int[] stampLayout(WorldGenLevel level, BoundingBox chunkBB) {
        int[] lowestSolid = new int[MoundLayout.SIZE_X * MoundLayout.SIZE_Z];
        Arrays.fill(lowestSolid, NO_COLUMN_HEIGHT);
        String data = MoundLayout.DATA;
        for (int index = 0; index + MoundLayout.ENTRY_CHARS <= data.length(); index += MoundLayout.ENTRY_CHARS) {
            int x = data.charAt(index) - CHAR_BASE;
            int y = data.charAt(index + 1) - CHAR_BASE;
            int z = data.charAt(index + 2) - CHAR_BASE;
            BlockState state = stateFor(data.charAt(index + 3) - CHAR_BASE);
            placeBlock(level, state.mirror(getMirror()), x, y, z, chunkBB);
            int column = x * MoundLayout.SIZE_Z + z;
            if (state.blocksMotion() && (lowestSolid[column] == NO_COLUMN_HEIGHT || y < lowestSolid[column])) {
                lowestSolid[column] = y;
            }
        }
        return lowestSolid;
    }

    private void buildFoundation(WorldGenLevel level, BoundingBox chunkBB, int[] lowestSolid) {
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        for (int x = 0; x < MoundLayout.SIZE_X; x++) {
            for (int z = 0; z < MoundLayout.SIZE_Z; z++) {
                int lowest = lowestSolid[x * MoundLayout.SIZE_Z + z];
                if (lowest == NO_COLUMN_HEIGHT) {
                    continue;
                }
                BlockPos below = getWorldPos(x, lowest - 1, z);
                if (chunkBB.isInside(below) && isReplaceableByStructures(level.getBlockState(below))) {
                    placeBlock(level, dirt, x, lowest - 1, z, chunkBB);
                    fillColumnDown(level, stone, x, lowest - 2, z, chunkBB);
                }
            }
        }
    }

    private void placeLoot(WorldGenLevel level, RandomSource random, BoundingBox chunkBB, int x, int y, int z) {
        int tierRoll = random.nextInt(PERCENT_ROLL);
        boolean crate = random.nextInt(PERCENT_ROLL) < CRATE_PERCENT;
        Block block;
        if (tierRoll < RARE_PERCENT) {
            block = (crate ? TTBlocks.LOOT_CRATE_RARE : TTBlocks.LOOT_URN_RARE).get();
        } else if (tierRoll < RARE_PERCENT + UNCOMMON_PERCENT) {
            block = (crate ? TTBlocks.LOOT_CRATE_UNCOMMON : TTBlocks.LOOT_URN_UNCOMMON).get();
        } else {
            block = (crate ? TTBlocks.LOOT_CRATE_COMMON : TTBlocks.LOOT_URN_COMMON).get();
        }
        placeBlock(level, block.defaultBlockState().mirror(getMirror()), x, y, z, chunkBB);
    }

    private void placeChest(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockPos pos = getWorldPos(CHEST_X, CHEST_Y, CHEST_Z).immutable();
        if (!chunkBB.isInside(pos)) {
            return;
        }
        boolean trapped = random.nextInt(TRAPPED_CHEST_ONE_IN) == 0;
        BlockState chest = (trapped ? Blocks.TRAPPED_CHEST : Blocks.CHEST).defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST);
        if (createChest(level, chunkBB, random, pos, BuiltInLootTables.SIMPLE_DUNGEON, chest) && trapped) {
            level.setBlock(pos.below(TNT_DEPTH), Blocks.TNT.defaultBlockState(), BLOCK_FLAGS);
        }
    }

    private void placeSpawner(WorldGenLevel level, RandomSource random, BoundingBox chunkBB, int x, int y, int z, EntityType<?> type) {
        BlockPos pos = getWorldPos(x, y, z);
        if (!chunkBB.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), BLOCK_FLAGS);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, random);
        }
    }

    private void spawnPortal(WorldGenLevel level, BoundingBox chunkBB) {
        BlockPos pos = getWorldPos(PORTAL_X, PORTAL_Y, PORTAL_Z);
        if (portalSpawned || !chunkBB.isInside(pos)) {
            return;
        }
        portalSpawned = true;
        EntityCultistPortalLesser portal = TTEntities.CULTIST_PORTAL_LESSER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (portal == null) {
            return;
        }
        portal.setPersistenceRequired();
        portal.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        portal.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        level.addFreshEntityWithPassengers(portal);
    }

    private void placeNode(WorldGenLevel level, RandomSource random, BoundingBox chunkBB) {
        BlockPos pos = getWorldPos(NODE_X, NODE_Y, NODE_Z).immutable();
        if (chunkBB.isInside(pos)) {
            NodeGenerator.createRandomNodeAt(level, pos, random, false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
        }
    }

    private static BlockState stateFor(int id) {
        return switch (id) {
            case MoundLayout.COBBLESTONE -> Blocks.COBBLESTONE.defaultBlockState();
            case MoundLayout.DIRT -> Blocks.DIRT.defaultBlockState();
            case MoundLayout.GRASS_BLOCK -> Blocks.GRASS_BLOCK.defaultBlockState();
            case MoundLayout.MOSSY_COBBLESTONE -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case MoundLayout.SHORT_GRASS -> Blocks.SHORT_GRASS.defaultBlockState();
            case MoundLayout.STAIRS_EAST -> stairs(Direction.EAST, Half.BOTTOM);
            case MoundLayout.STAIRS_NORTH -> stairs(Direction.NORTH, Half.BOTTOM);
            case MoundLayout.STAIRS_WEST -> stairs(Direction.WEST, Half.BOTTOM);
            case MoundLayout.STAIRS_SOUTH -> stairs(Direction.SOUTH, Half.BOTTOM);
            case MoundLayout.STAIRS_EAST_TOP -> stairs(Direction.EAST, Half.TOP);
            case MoundLayout.STAIRS_WEST_TOP -> stairs(Direction.WEST, Half.TOP);
            case MoundLayout.STAIRS_SOUTH_TOP -> stairs(Direction.SOUTH, Half.TOP);
            case MoundLayout.STAIRS_NORTH_TOP -> stairs(Direction.NORTH, Half.TOP);
            case MoundLayout.LADDER_EAST -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST);
            case MoundLayout.IRON_BARS -> Blocks.IRON_BARS.defaultBlockState();
            case MoundLayout.CHISELED_STONE_BRICKS -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.AIR.defaultBlockState();
        };
    }

    private static BlockState stairs(Direction facing, Half half) {
        return Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }
}
