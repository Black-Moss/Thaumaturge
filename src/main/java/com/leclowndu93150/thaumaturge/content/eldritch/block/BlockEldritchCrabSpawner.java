package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchCrabSpawner extends BaseEntityBlock {
    public static final MapCodec<BlockEldritchCrabSpawner> CODEC = simpleCodec(BlockEldritchCrabSpawner::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final int EXP_BASE = 15;
    private static final int EXP_SPREAD = 15;
    private static final int[][] PLATE_BOXES = {{0, 0, 0, 7, 1, 16}, {1, 1, 5, 5, 2, 11}, {2, 1, 3, 14, 2, 5}, {2, 1, 11, 14, 2, 14}, {2, 2, 5, 4, 3, 11}, {3, 1, 2, 14, 2, 3}, {3, 2, 3, 7, 3, 5},
            {3, 2, 11, 7, 3, 13}, {4, 1, 14, 11, 2, 15}, {4, 2, 5, 5, 3, 6}, {4, 2, 10, 5, 3, 11}, {5, 1, 1, 11, 2, 2}, {5, 1, 5, 6, 2, 6}, {5, 1, 10, 6, 2, 11}, {5, 2, 2, 10, 3, 3},
            {5, 2, 13, 10, 3, 14}, {7, 0, 0, 16, 1, 7}, {7, 0, 9, 16, 1, 16}, {7, 2, 3, 13, 3, 4}, {7, 2, 12, 13, 3, 13}, {9, 0, 7, 16, 1, 9}, {9, 2, 11, 13, 3, 12}, {10, 1, 5, 15, 2, 6},
            {10, 1, 10, 15, 2, 11}, {10, 2, 4, 13, 3, 5}, {11, 1, 6, 15, 2, 10}, {11, 2, 5, 14, 3, 6}, {11, 2, 10, 13, 3, 11}, {12, 2, 6, 13, 3, 10}, {13, 2, 6, 14, 3, 9}};
    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes.facingShapesFromUp(plate());

    public BlockEldritchCrabSpawner(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    private static VoxelShape plate() {
        VoxelShape shape = Shapes.empty();
        for (int[] box : PLATE_BOXES) {
            shape = Shapes.or(shape, Block.box(box[0], box[1], box[2], box[3], box[4], box[5]));
        }
        return shape;
    }

    @Override
    protected MapCodec<BlockEldritchCrabSpawner> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        RandomSource random = level.getRandom();
        return EXP_BASE + random.nextInt(EXP_SPREAD) + random.nextInt(EXP_SPREAD);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchCrabSpawner(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), (tickLevel, pos, tickState, vent) -> vent.tick(tickLevel, pos, tickState));
    }
}
