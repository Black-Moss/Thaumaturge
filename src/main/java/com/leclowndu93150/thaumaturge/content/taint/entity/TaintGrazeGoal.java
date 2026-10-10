package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintGrazeGoal extends Goal {
    private static final int GRAZE_ODDS = 250;
    private static final int GRAZE_TICKS = 40;
    private static final int BITE_AT_REMAINING = 4;

    private final Sheep sheep;
    private int grazeTicksLeft;

    public TaintGrazeGoal(Sheep sheep) {
        this.sheep = sheep;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return sheep.getRandom().nextInt(reducedTickDelay(GRAZE_ODDS)) == 0 && pasture() != Pasture.NONE;
    }

    @Override
    public boolean canContinueToUse() {
        return grazeTicksLeft > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        grazeTicksLeft = GRAZE_TICKS;
        sheep.getNavigation().stop();
        sheep.level().broadcastEntityEvent(sheep, EntityEvent.EAT_GRASS);
    }

    @Override
    public void stop() {
        grazeTicksLeft = 0;
    }

    @Override
    public void tick() {
        grazeTicksLeft--;
        if (grazeTicksLeft == BITE_AT_REMAINING && sheep.level() instanceof ServerLevel level && bite(level)) {
            spreadTaint(level, sheep.blockPosition());
            sheep.ate();
        }
    }

    private Pasture pasture() {
        Level level = sheep.level();
        BlockPos feet = sheep.blockPosition();
        if (level.getBlockState(feet).is(Blocks.SHORT_GRASS)) {
            return Pasture.TUFT;
        }
        return level.getBlockState(feet.below()).is(Blocks.GRASS_BLOCK) ? Pasture.TURF : Pasture.NONE;
    }

    private boolean bite(ServerLevel level) {
        BlockPos feet = sheep.blockPosition();
        switch (pasture()) {
            case TUFT -> level.destroyBlock(feet, false, sheep);
            case TURF -> {
                BlockPos below = feet.below();
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, below, Block.getId(level.getBlockState(below)));
            }
            case NONE -> {
                return false;
            }
        }
        return true;
    }

    private static void spreadTaint(ServerLevel level, BlockPos feet) {
        TaintBiomeManager.taintColumn(level, feet);
        BlockState here = level.getBlockState(feet);
        if (here.canBeReplaced() && here.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, feet)) {
            level.setBlock(feet, BlockTaintFibre.stateForWorld(level, feet), Block.UPDATE_ALL);
        }
    }

    private enum Pasture {
        TUFT, TURF, NONE
    }
}
