package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

public final class VolleyGoal extends Goal {
    private static final int UNSET = -1;
    private static final int MIN_SIGHT_TICKS = 20;
    private static final float MAX_LOOK_YAW = 10.0F;
    private static final float MAX_LOOK_PITCH = 30.0F;
    private static final float MIN_POWER = 0.1F;
    private static final float MAX_POWER = 1.0F;
    private static final float IN_RANGE = 1.0F;

    private final Rig rig;
    private final Engagement engagement;
    private @Nullable LivingEntity target;

    public VolleyGoal(RangedAttackMob shooter, double speed, int reloadMin, int reloadMax, float radius) {
        rig = new Rig(asMob(shooter), shooter, speed, radius);
        engagement = new Engagement(reloadMin, reloadMax);
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return acquireTarget();
    }

    @Override
    public boolean canContinueToUse() {
        boolean tracking = acquireTarget();
        return tracking || !rig.mob().getNavigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        engagement.reset();
    }

    @Override
    public void tick() {
        LivingEntity quarry = target;
        if (quarry == null) {
            return;
        }
        boolean visible = rig.mob().getSensing().hasLineOfSight(quarry);
        engagement.observe(visible);
        float reach = rangeRatio(quarry);
        steer(quarry, reach <= IN_RANGE && engagement.hasSteadySight());
        rig.mob().getLookControl().setLookAt(quarry, MAX_LOOK_YAW, MAX_LOOK_PITCH);
        if (engagement.advance(reach, visible && reach <= IN_RANGE)) {
            rig.shooter().performRangedAttack(quarry, Mth.clamp(reach, MIN_POWER, MAX_POWER));
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private static Mob asMob(RangedAttackMob shooter) {
        if (shooter instanceof Mob owner) {
            return owner;
        }
        throw new IllegalArgumentException("VolleyGoal needs a shooter that is also a Mob");
    }

    private boolean acquireTarget() {
        LivingEntity found = rig.mob().getTarget();
        if (found != null) {
            target = found;
        }
        return found != null;
    }

    private float rangeRatio(LivingEntity quarry) {
        double sqr = rig.mob().distanceToSqr(quarry.getX(), quarry.getBoundingBox().minY, quarry.getZ());
        return (float) Math.sqrt(sqr) / rig.radius();
    }

    private void steer(LivingEntity quarry, boolean hold) {
        PathNavigation navigation = rig.mob().getNavigation();
        if (!hold) {
            navigation.moveTo(quarry, rig.speed());
            return;
        }
        navigation.stop();
    }

    private record Rig(Mob mob, RangedAttackMob shooter, double speed, float radius) {
    }

    private static final class Engagement {
        private final int min;
        private final int max;
        private int remaining = UNSET;
        private int sightTicks;

        private Engagement(int min, int max) {
            this.min = min;
            this.max = max;
        }

        private void reset() {
            sightTicks = 0;
            remaining = UNSET;
        }

        private void observe(boolean visible) {
            sightTicks = visible ? sightTicks + 1 : 0;
        }

        private boolean hasSteadySight() {
            return sightTicks >= MIN_SIGHT_TICKS;
        }

        private boolean advance(float reach, boolean mayFire) {
            remaining--;
            if (remaining < 0) {
                remaining = spacing(reach);
                return false;
            }
            if (remaining != 0 || !mayFire) {
                return false;
            }
            remaining = spacing(reach);
            return true;
        }

        private int spacing(float reach) {
            return Mth.floor(reach * (max - min) + min);
        }
    }
}
