package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockTaintLog extends RotatedPillarBlock implements ITaintBlock {
    public static final MapCodec<BlockTaintLog> CODEC = simpleCodec(BlockTaintLog::new);

    private static final int DECAY_ONE_IN = 10;

    public BlockTaintLog(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockTaintLog> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (!TaintHelper.isEcologicallySustained(level, pos) && random.nextInt(DECAY_ONE_IN) == 0) {
            decay(level, pos, state);
            return;
        }
        TaintHelper.attemptFibreGrowth(level, pos, false);
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, FluxGooFluid.gooBlockState(PhysicalFlux.MAX_QUANTA), Block.UPDATE_ALL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withAxis(context.getClickedFace().getAxis());
    }

    public BlockState withAxis(Direction.Axis axis) {
        return defaultBlockState().setValue(AXIS, axis);
    }
}
