package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintGrazeGoal extends Goal {
    private static final int START_ONE_IN = 250;
    private static final int GRAZE_TICKS = 40;
    private static final int CONVERT_AT_TICK = 4;
    private static final byte EAT_ANIMATION_EVENT = 10;

    private final Sheep sheep;
    private int grazeTicks;

    public TaintGrazeGoal(Sheep sheep) {
        this.sheep = sheep;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        Level level = sheep.level();
        if (level.isClientSide() || sheep.getRandom().nextInt(START_ONE_IN) != 0) {
            return false;
        }
        BlockPos pos = sheep.blockPosition();
        return level.getBlockState(pos).is(Blocks.SHORT_GRASS) || level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
    }

    @Override
    public void start() {
        grazeTicks = GRAZE_TICKS;
        sheep.level().broadcastEntityEvent(sheep, EAT_ANIMATION_EVENT);
        sheep.getNavigation().stop();
    }

    @Override
    public void stop() {
        grazeTicks = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return grazeTicks > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        grazeTicks = Math.max(0, grazeTicks - 1);
        if (grazeTicks == CONVERT_AT_TICK && sheep.level() instanceof ServerLevel level) {
            graze(level);
        }
    }

    private void graze(ServerLevel level) {
        BlockPos pos = sheep.blockPosition();
        if (level.getBlockState(pos).is(Blocks.SHORT_GRASS)) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
            level.removeBlock(pos, false);
        } else if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.below(), Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
        } else {
            return;
        }
        TaintBiomeManager.taintColumn(level, pos);
        BlockState state = level.getBlockState(pos);
        if (state.canBeReplaced() && state.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, pos)) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        sheep.ate();
    }
}
