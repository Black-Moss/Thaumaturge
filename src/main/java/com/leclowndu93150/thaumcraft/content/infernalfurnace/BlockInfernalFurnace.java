package com.leclowndu93150.thaumcraft.content.infernalfurnace;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockInfernalFurnace extends BaseEntityBlock {

    private static final MapCodec<BlockInfernalFurnace> CODEC = simpleCodec(BlockInfernalFurnace::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockInfernalFurnace(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.box(0,0,0,1,0.5,1);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    protected static void destroyFurnace(LevelAccessor level, BlockPos pos, BlockState state, BlockPos startpos) {
        if (level.isClientSide()) {
            return;
        }
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                for (int c = -1; c <= 1; c++) {
                    BlockPos shellPos = pos.offset(a, b, c);
                    if (shellPos.equals(startpos)) {
                        continue;
                    }
                    BlockState bs = level.getBlockState(shellPos);
                    if (bs.is(TCBlocks.NETHER_BRICKS_PLACEHOLDER)) {
                        level.setBlock(shellPos, Blocks.NETHER_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (bs.is(TCBlocks.OBSIDIAN_PLACEHOLDER)) {
                        level.setBlock(shellPos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        BlockPos barsPos = pos.relative(state.getValue(FACING).getOpposite());
        if (level.isEmptyBlock(barsPos) && !barsPos.equals(startpos)) {
            level.setBlock(barsPos, Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        destroyFurnace(level,pos,state,pos);
        super.destroy(level, pos, state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING,mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }
}
