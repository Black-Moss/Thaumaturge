package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockCondenser extends BaseEntityBlock {
    public static final MapCodec<BlockCondenser> CODEC = simpleCodec(BlockCondenser::new);

    private static final VoxelShape FOOT = Block.box(6.0, 0.0, 6.0, 10.0, 2.0, 10.0);
    private static final VoxelShape LOWER_PLATE = Block.box(4.0, 2.0, 4.0, 12.0, 3.0, 12.0);
    private static final VoxelShape UPPER_PLATE = Block.box(4.0, 13.0, 4.0, 12.0, 14.0, 12.0);
    private static final VoxelShape COLUMN = Block.box(5.0, 3.0, 5.0, 11.0, 13.0, 11.0);
    private static final VoxelShape LOWER_FIN = Block.box(2.0, 3.0, 2.0, 14.0, 5.0, 14.0);
    private static final VoxelShape MIDDLE_FIN = Block.box(2.0, 7.0, 2.0, 14.0, 9.0, 14.0);
    private static final VoxelShape UPPER_FIN = Block.box(2.0, 11.0, 2.0, 14.0, 13.0, 14.0);
    private static final VoxelShape PORT_NORTH = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 2.0);
    private static final VoxelShape PORT_SOUTH = Block.box(6.0, 6.0, 14.0, 10.0, 10.0, 16.0);
    private static final VoxelShape PORT_WEST = Block.box(0.0, 6.0, 6.0, 2.0, 10.0, 10.0);
    private static final VoxelShape PORT_EAST = Block.box(14.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape CAP = Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape SHAPE = Shapes.or(FOOT, LOWER_PLATE, UPPER_PLATE, COLUMN, LOWER_FIN, MIDDLE_FIN, UPPER_FIN, PORT_NORTH, PORT_SOUTH, PORT_WEST, PORT_EAST, CAP).optimize();

    public BlockCondenser(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.ENABLED, true));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<BlockCondenser> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.ENABLED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(BlockStateProperties.ENABLED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, enabled), Block.UPDATE_ALL);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityCondenser(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.CONDENSER.get(), BlockEntityCondenser::serverTick);
    }
}
