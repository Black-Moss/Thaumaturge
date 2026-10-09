package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockEntityLampArcane extends BlockEntity {
    private static final int PLACE_INTERVAL = 5;
    private static final int OFFSET_SPAN = 16;
    private static final int LIGHT_RADIUS = 15;
    private static final int SURFACE_CEILING = 4;
    private static final int FLOOR_MARGIN = 5;
    private static final int MAX_LIGHT_LEVEL = 11;

    private final BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();

    public BlockEntityLampArcane(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LAMP_ARCANE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampArcane lamp) {
        boolean lit = state.getValue(BlockStateProperties.ENABLED);
        if (level.hasNeighborSignal(pos)) {
            if (lit) {
                BlockLamp.showLit(level, pos, state, false);
                lamp.removeLights();
            }
            return;
        }
        if (!lit) {
            BlockLamp.showLit(level, pos, state, true);
        }
        if (level.getGameTime() % PLACE_INTERVAL == 0) {
            lamp.tryPlaceGlimmer(level, pos);
        }
    }

    private static int spread(RandomSource random) {
        return random.nextInt(OFFSET_SPAN) - random.nextInt(OFFSET_SPAN);
    }

    private void tryPlaceGlimmer(Level level, BlockPos origin) {
        RandomSource random = level.getRandom();
        int x = origin.getX() + spread(random);
        int y = origin.getY() + spread(random);
        int z = origin.getZ() + spread(random);
        target.set(x, y, z);
        if (!level.hasChunkAt(target)) {
            return;
        }
        target.setY(adjustHeight(level, y));
        if (!isFreeDarkCell(level) || !hasClearLine(level, origin, target)) {
            return;
        }
        level.setBlock(target.immutable(), TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    private int adjustHeight(Level level, int wanted) {
        int surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target).getY();
        int lowered = Math.min(wanted, surface + SURFACE_CEILING);
        return Math.max(level.getMinY() + FLOOR_MARGIN, lowered);
    }

    private boolean isFreeDarkCell(Level level) {
        return level.getBlockState(target).isAir() && level.getBrightness(LightLayer.BLOCK, target) < MAX_LIGHT_LEVEL;
    }

    private static boolean hasClearLine(Level level, BlockPos origin, BlockPos end) {
        Vec3 from = Vec3.atCenterOf(origin);
        Vec3 to = Vec3.atCenterOf(end);
        BlockPos goal = end.immutable();
        Boolean clear = BlockGetter.traverseBlocks(from, to, level, (getter, cell) -> blocksRay(getter, cell, origin, goal, from, to) ? Boolean.FALSE : null, getter -> Boolean.TRUE);
        return clear;
    }

    private static boolean blocksRay(BlockGetter getter, BlockPos cell, BlockPos origin, BlockPos goal, Vec3 from, Vec3 to) {
        if (cell.equals(origin) || cell.equals(goal)) {
            return false;
        }
        VoxelShape shape = getter.getBlockState(cell).getCollisionShape(getter, cell);
        return !shape.isEmpty() && shape.clip(from, to, cell) != null;
    }

    public void removeLights() {
        if (level == null || level.isClientSide()) {
            return;
        }
        Block glimmer = TTBlocks.EFFECT_GLIMMER.get();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos low = worldPosition.offset(-LIGHT_RADIUS, -LIGHT_RADIUS, -LIGHT_RADIUS);
        BlockPos high = worldPosition.offset(LIGHT_RADIUS, LIGHT_RADIUS, LIGHT_RADIUS);
        for (BlockPos cell : BlockPos.betweenClosed(low, high)) {
            if (!level.hasChunkAt(cell) || !level.getBlockState(cell).is(glimmer)) {
                continue;
            }
            level.setBlock(cell.immutable(), air, Block.UPDATE_ALL);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        removeLights();
        super.preRemoveSideEffects(pos, state);
    }
}
