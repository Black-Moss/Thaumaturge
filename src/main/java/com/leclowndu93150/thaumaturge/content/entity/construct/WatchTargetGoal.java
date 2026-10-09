package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

public final class WatchTargetGoal extends Goal {
    private static final int MIN_LOOK_TICKS = 40;
    private static final int LOOK_TICK_SPREAD = 40;
    private static final float YAW_SPEED = 10.0F;

    private @Nullable LivingEntity watched;
    private int lookTime;
    private final Mob mob;

    public WatchTargetGoal(Mob mob) {
        setFlags(EnumSet.of(Goal.Flag.LOOK));
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        remember(mob.getTarget());
        return watched != null;
    }

    private void remember(@Nullable LivingEntity candidate) {
        if (candidate != null) {
            watched = candidate;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (lookTime <= 0 || watched == null) {
            return false;
        }
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        return watched.isAlive() && mob.distanceToSqr(watched) <= range * range;
    }

    @Override
    public void start() {
        lookTime = MIN_LOOK_TICKS + mob.getRandom().nextInt(LOOK_TICK_SPREAD);
    }

    @Override
    public void tick() {
        LivingEntity subject = watched;
        if (subject != null) {
            double eyeY = subject.getY() + subject.getEyeHeight();
            mob.getLookControl().setLookAt(subject.getX(), eyeY, subject.getZ(), YAW_SPEED, mob.getMaxHeadXRot());
        }
        lookTime--;
    }

    @Override
    public void stop() {
        watched = null;
    }
}
