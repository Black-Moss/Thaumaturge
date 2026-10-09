package com.leclowndu93150.thaumaturge.content.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public final class LongRangeAttackGoal extends RangedAttackGoal {
    private final Mob shooter;
    private final double minDistanceSqr;

    public LongRangeAttackGoal(RangedAttackMob mob, double speed, double minDistance, int minInterval, int maxInterval, float radius) {
        super(mob, speed, minInterval, maxInterval, radius);
        this.minDistanceSqr = square(minDistance);
        this.shooter = requireMob(mob);
    }

    private static double square(double value) {
        return value * value;
    }

    private static Mob requireMob(RangedAttackMob candidate) {
        if (!(candidate instanceof Mob mob)) {
            throw new IllegalArgumentException("LongRangeAttackGoal requires a Mob that implements RangedAttackMob");
        }
        return mob;
    }

    private boolean hasLiveTarget() {
        LivingEntity target = shooter.getTarget();
        if (target != null && !target.isAlive()) {
            shooter.setTarget(null);
            return false;
        }
        return target != null;
    }

    private boolean isBeyondMinimum() {
        return shooter.distanceToSqr(shooter.getTarget()) >= minDistanceSqr;
    }

    @Override
    public boolean canUse() {
        return hasLiveTarget() && isBeyondMinimum() && super.canUse();
    }
}
