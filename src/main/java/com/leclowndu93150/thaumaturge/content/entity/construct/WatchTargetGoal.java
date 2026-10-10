package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

public final class WatchTargetGoal extends Goal {
    private static final int MIN_WATCH_TICKS = 40;
    private static final int EXTRA_WATCH_TICKS = 41;
    private static final float YAW_TURN_SPEED = 30.0F;
    private static final double FOLLOW_RANGE_SQR = EntityTurretCrossbow.FOLLOW_RANGE * EntityTurretCrossbow.FOLLOW_RANGE;

    private final Mob watcher;
    private @Nullable LivingEntity watched;
    private int remainingTicks;

    public WatchTargetGoal(Mob watcher) {
        this.watcher = watcher;
        setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = watcher.getTarget();
        if (target == null) {
            return false;
        }
        watched = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity subject = watched;
        return remainingTicks > 0 && subject != null && subject.isAlive() && watcher.distanceToSqr(subject) <= FOLLOW_RANGE_SQR;
    }

    @Override
    public void start() {
        remainingTicks = MIN_WATCH_TICKS + watcher.getRandom().nextInt(EXTRA_WATCH_TICKS);
    }

    @Override
    public void stop() {
        watched = null;
        remainingTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity subject = watched;
        if (subject == null) {
            return;
        }
        remainingTicks--;
        watcher.getLookControl().setLookAt(subject.getX(), subject.getEyeY(), subject.getZ(), YAW_TURN_SPEED, watcher.getMaxHeadXRot());
    }
}
