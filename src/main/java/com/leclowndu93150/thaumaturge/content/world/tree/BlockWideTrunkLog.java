package com.leclowndu93150.thaumaturge.content.world.tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public final class BlockWideTrunkLog extends RotatedPillarBlock {
    public static final MapCodec<BlockWideTrunkLog> CODEC = simpleCodec(BlockWideTrunkLog::new);

    public BlockWideTrunkLog(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TrunkQuadrant.PROPERTY, TrunkQuadrant.SINGLE));
    }

    @Override
    public MapCodec<BlockWideTrunkLog> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TrunkQuadrant.PROPERTY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return resolveQuadrant(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        if (directionToNeighbour.getAxis().isVertical()) {
            return updated;
        }
        return resolveQuadrant(updated, level, pos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = super.rotate(state, rotation);
        return rotated.setValue(TrunkQuadrant.PROPERTY, state.getValue(TrunkQuadrant.PROPERTY).rotated(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(TrunkQuadrant.PROPERTY, state.getValue(TrunkQuadrant.PROPERTY).mirrored(mirror));
    }

    private BlockState resolveQuadrant(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(AXIS) != Direction.Axis.Y) {
            return state.setValue(TrunkQuadrant.PROPERTY, TrunkQuadrant.SINGLE);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (!horizontalNeighboursLoaded(level, pos, cursor)) {
            return state;
        }
        return state.setValue(TrunkQuadrant.PROPERTY, deriveQuadrant(level, pos, cursor));
    }

    private static boolean horizontalNeighboursLoaded(LevelReader level, BlockPos pos, BlockPos.MutableBlockPos cursor) {
        return level.hasChunkAt(cursor.setWithOffset(pos, Direction.EAST)) && level.hasChunkAt(cursor.setWithOffset(pos, Direction.WEST))
                && level.hasChunkAt(cursor.setWithOffset(pos, Direction.SOUTH)) && level.hasChunkAt(cursor.setWithOffset(pos, Direction.NORTH));
    }

    private TrunkQuadrant deriveQuadrant(LevelReader level, BlockPos pos, BlockPos.MutableBlockPos cursor) {
        boolean east = isTrunkNeighbour(level, cursor.setWithOffset(pos, Direction.EAST));
        boolean west = isTrunkNeighbour(level, cursor.setWithOffset(pos, Direction.WEST));
        boolean south = isTrunkNeighbour(level, cursor.setWithOffset(pos, Direction.SOUTH));
        boolean north = isTrunkNeighbour(level, cursor.setWithOffset(pos, Direction.NORTH));
        if (east && south) {
            return TrunkQuadrant.NORTH_WEST;
        }
        if (west && south) {
            return TrunkQuadrant.NORTH_EAST;
        }
        if (east && north) {
            return TrunkQuadrant.SOUTH_WEST;
        }
        if (west && north) {
            return TrunkQuadrant.SOUTH_EAST;
        }
        return TrunkQuadrant.SINGLE;
    }

    private boolean isTrunkNeighbour(LevelReader level, BlockPos neighbour) {
        BlockState neighbourState = level.getBlockState(neighbour);
        return neighbourState.is(this) && neighbourState.getValue(AXIS) == Direction.Axis.Y;
    }
}
