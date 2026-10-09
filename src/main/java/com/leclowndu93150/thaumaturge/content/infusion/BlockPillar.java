package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockPillar extends HorizontalDirectionalBlock {
    public static final MapCodec<BlockPillar> CODEC = simpleCodec(BlockPillar::new);
    private static final float FULL_BRIGHTNESS = 1.0F;
    private static final double[][] NORTH_BOXES = {{0, 0, 0, 16, 8, 16}, {3, 8, 3, 13, 19, 13}, {5, 19, 5, 12, 20, 12}, {4, 20, 4, 12, 23, 12}, {4, 21, 12, 13, 23, 13}, {12, 22, 5, 13, 25, 13},
            {5, 23, 5, 7, 27, 13}, {7, 23, 5, 12, 25, 13}, {7, 25, 5, 13, 26, 13}, {12, 25, 13, 14, 26, 14}, {7, 26, 5, 10, 27, 8}, {7, 26, 7, 15, 28, 15}, {10, 27, 15, 15, 31, 16},
            {7, 28, 10, 15, 29, 15}, {8, 28, 8, 15, 30, 10}, {15, 28, 12, 16, 30, 14}, {8, 29, 9, 16, 31, 15}, {14, 30, 11, 15, 32, 13}, {10, 31, 10, 14, 32, 15}, {11, 32, 11, 13, 33, 14}};
    private static final int BOX_MIN_X = 0;
    private static final int BOX_MIN_Y = 1;
    private static final int BOX_MIN_Z = 2;
    private static final int BOX_MAX_X = 3;
    private static final int BOX_MAX_Y = 4;
    private static final int BOX_MAX_Z = 5;
    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    public BlockPillar(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        VoxelShape north = Shapes.empty();
        for (double[] box : NORTH_BOXES) {
            north = Shapes.or(north, Block.box(box[BOX_MIN_X], box[BOX_MIN_Y], box[BOX_MIN_Z], box[BOX_MAX_X], box[BOX_MAX_Y], box[BOX_MAX_Z]));
        }
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.EAST, DeviceShapes.rotate(north, 0, 1));
        shapes.put(Direction.SOUTH, DeviceShapes.rotate(north, 0, 2));
        shapes.put(Direction.WEST, DeviceShapes.rotate(north, 0, 3));
        return shapes;
    }

    @Override
    protected MapCodec<BlockPillar> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return FULL_BRIGHTNESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
