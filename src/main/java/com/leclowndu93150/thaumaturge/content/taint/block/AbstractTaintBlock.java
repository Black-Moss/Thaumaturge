package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintExposure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractTaintBlock extends Block implements ITaintBlock {
    private static final int DECAY_ONE_IN = 10;
    private static final int STEP_INFECTION_ONE_IN = 250;

    protected AbstractTaintBlock(Properties properties) {
        super(properties);
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
        subRandomTick(state, level, pos, random);
    }

    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        FluxTaintExposure.onStep(level, entity, STEP_INFECTION_ONE_IN);
    }
}
