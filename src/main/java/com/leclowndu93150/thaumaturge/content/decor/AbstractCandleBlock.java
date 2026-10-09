package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCandleBlock extends Block implements IInfusionStabiliser {
    private static final ParticleOptions[] FLAME_PARTICLES = {ParticleTypes.SMOKE, ParticleTypes.FLAME};

    private final double flameYOffset;

    protected AbstractCandleBlock(double flameYOffset, Properties properties) {
        super(properties);
        this.flameYOffset = flameYOffset;
    }

    protected abstract boolean isBurning(BlockState state);

    @Override
    public boolean canStabiliseInfusion(Level level, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos support = pos.below();
        return level.getBlockState(support).isFaceSturdy(level, support, Direction.UP, SupportType.CENTER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction != Direction.DOWN || canSurvive(state, level, pos)) {
            return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (isBurning(state)) {
            Vec3 wick = new Vec3(pos.getX() + 0.5, pos.getY() + flameYOffset, pos.getZ() + 0.5);
            for (ParticleOptions particle : FLAME_PARTICLES) {
                level.addParticle(particle, wick.x, wick.y, wick.z, 0.0, 0.0, 0.0);
            }
        }
    }
}
