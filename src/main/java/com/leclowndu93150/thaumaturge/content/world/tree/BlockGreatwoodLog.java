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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public final class BlockGreatwoodLog extends RotatedPillarBlock {
    public static final MapCodec<BlockGreatwoodLog> CODEC = simpleCodec(BlockGreatwoodLog::new);

    public BlockGreatwoodLog(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(TrunkCorner.PROPERTY, TrunkCorner.NONE));
    }

    @Override
    public MapCodec<BlockGreatwoodLog> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TrunkCorner.PROPERTY);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(TrunkCorner.PROPERTY, cornerAt(context.getLevel(), context.getClickedPos(), state));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour.getAxis().isHorizontal()) {
            return state.setValue(TrunkCorner.PROPERTY, cornerAt(level, pos, state));
        }
        return state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation).setValue(TrunkCorner.PROPERTY, state.getValue(TrunkCorner.PROPERTY).rotate(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(TrunkCorner.PROPERTY, state.getValue(TrunkCorner.PROPERTY).mirror(mirror));
    }

    private TrunkCorner cornerAt(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(AXIS) != Direction.Axis.Y) {
            return TrunkCorner.NONE;
        }
        boolean east = isTrunk(level.getBlockState(pos.east()));
        boolean west = isTrunk(level.getBlockState(pos.west()));
        boolean south = isTrunk(level.getBlockState(pos.south()));
        boolean north = isTrunk(level.getBlockState(pos.north()));
        if (east && south) {
            return TrunkCorner.NORTH_WEST;
        }
        if (west && south) {
            return TrunkCorner.NORTH_EAST;
        }
        if (east && north) {
            return TrunkCorner.SOUTH_WEST;
        }
        if (west && north) {
            return TrunkCorner.SOUTH_EAST;
        }
        return TrunkCorner.NONE;
    }

    private boolean isTrunk(BlockState state) {
        return state.is(this) && state.getValue(AXIS) == Direction.Axis.Y;
    }
}
