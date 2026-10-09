package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

public final class AltarFocusGoal extends Goal {
    private static final int CHECK_INTERVAL = 40;
    private static final double ANCHOR_RANGE = 4.0;
    private static final double ANCHOR_RANGE_SQR = ANCHOR_RANGE * ANCHOR_RANGE;
    private static final EnumSet<Goal.Flag> FLAGS = EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE);

    private int phase;
    private final EntityCultistCleric priest;

    public AltarFocusGoal(EntityCultistCleric priest) {
        setFlags(FLAGS);
        this.priest = priest;
    }

    @Override
    public boolean canContinueToUse() {
        return priest.isRitualist();
    }

    @Override
    public boolean canUse() {
        return priest.isRitualist() && priest.hasHome();
    }

    private boolean isAtAltar() {
        if (!priest.hasHome()) {
            return false;
        }
        BlockPos altar = priest.getHomePosition();
        if (priest.blockPosition().distSqr(altar) > ANCHOR_RANGE_SQR) {
            return false;
        }
        return priest.level().getBlockState(altar).is(TTBlocks.ELDRITCH_ALTAR);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        phase = (phase + 1) % CHECK_INTERVAL;
        if (phase == 0 && !isAtAltar()) {
            priest.setRitualist(false);
        }
    }
}
