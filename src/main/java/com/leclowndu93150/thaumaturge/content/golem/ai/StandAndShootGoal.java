package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

public final class StandAndShootGoal extends Goal {
    private static final double WALK_SPEED = 1.0D;
    private static final float POWER_FLOOR = 0.1F;
    private static final float POWER_CEILING = 1.0F;
    private static final float FULL_RANGE = 1.0F;
    private static final float HEAD_TURN_SPEED = 30.0F;
    private static final int UNSCHEDULED = -1;

    private final Mob shooter;
    private final RangedAttackMob trigger;
    private final ShootingCadence cadence;
    private int sightTicks;
    private int shotCountdown = UNSCHEDULED;

    public StandAndShootGoal(Mob shooter, RangedAttackMob trigger, ShootingCadence cadence) {
        this.shooter = shooter;
        this.trigger = trigger;
        this.cadence = cadence;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return liveTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return liveTarget() != null || !shooter.getNavigation().isDone();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        sightTicks = 0;
        LivingEntity target = liveTarget();
        shotCountdown = target == null ? UNSCHEDULED : cadence.interval(rangeFraction(target));
    }

    @Override
    public void stop() {
        sightTicks = 0;
        shotCountdown = UNSCHEDULED;
    }

    @Override
    public void tick() {
        LivingEntity target = liveTarget();
        if (target == null) {
            return;
        }
        boolean visible = shooter.getSensing().hasLineOfSight(target);
        sightTicks = visible ? sightTicks + 1 : 0;
        float fraction = rangeFraction(target);
        boolean inRange = fraction <= FULL_RANGE;
        if (inRange && sightTicks >= cadence.sightTicks()) {
            shooter.getNavigation().stop();
        } else {
            shooter.getNavigation().moveTo(target, WALK_SPEED);
        }
        shooter.getLookControl().setLookAt(target, HEAD_TURN_SPEED, HEAD_TURN_SPEED);
        advanceCountdown(target, fraction, inRange && visible);
    }

    private void advanceCountdown(LivingEntity target, float fraction, boolean clearShot) {
        if (shotCountdown == UNSCHEDULED) {
            shotCountdown = cadence.interval(fraction);
            return;
        }
        if (--shotCountdown > 0) {
            return;
        }
        if (!clearShot) {
            shotCountdown = UNSCHEDULED;
            return;
        }
        trigger.performRangedAttack(target, Mth.clamp(fraction, POWER_FLOOR, POWER_CEILING));
        shotCountdown = cadence.interval(fraction);
    }

    private float rangeFraction(LivingEntity target) {
        return (float) (Math.sqrt(shooter.distanceToSqr(target)) / cadence.range());
    }

    private @Nullable LivingEntity liveTarget() {
        LivingEntity target = shooter.getTarget();
        return target != null && target.isAlive() ? target : null;
    }
}
