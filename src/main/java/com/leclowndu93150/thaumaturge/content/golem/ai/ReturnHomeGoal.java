package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ReturnHomeGoal extends Goal {
    private static final int FIRST_CHECK_DELAY = 10;
    private static final int CHECK_INTERVAL = 50;
    private static final double ARRIVED_SQR = 5.0D;
    private static final double SETTLED_SQR = 3.0D;
    private static final double FAR_SQR = 1024.0D;
    private static final int DETOUR_HORIZONTAL = 16;
    private static final int DETOUR_VERTICAL = 7;
    private static final double QUARTER_TURN = Math.PI / 2.0D;

    private final EntityThaumaturgeGolem golem;
    private @Nullable Vec3 destination;
    private long nextCheck;

    public ReturnHomeGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        scheduleCheck(FIRST_CHECK_DELAY);
    }

    @Override
    public boolean canUse() {
        long now = golem.level().getGameTime();
        if (now < nextCheck) {
            return false;
        }
        scheduleCheck(CHECK_INTERVAL);
        if (!golem.hasHome() || golem.activeJob() != null || !golem.getNavigation().isDone()) {
            return false;
        }
        Vec3 center = homeCenter();
        double distance = golem.distanceToSqr(center);
        if (distance <= ARRIVED_SQR) {
            return false;
        }
        destination = distance > FAR_SQR ? DefaultRandomPos.getPosTowards(golem, DETOUR_HORIZONTAL, DETOUR_VERTICAL, center, QUARTER_TURN) : center;
        return destination != null;
    }

    @Override
    public void start() {
        Vec3 target = destination;
        if (target != null) {
            golem.getNavigation().moveTo(target.x, target.y, target.z, golem.travelSpeed());
        }
    }

    @Override
    public boolean canContinueToUse() {
        return golem.activeJob() == null && !golem.getNavigation().isDone() && golem.distanceToSqr(homeCenter()) > SETTLED_SQR;
    }

    @Override
    public void stop() {
        golem.getNavigation().stop();
        scheduleCheck(CHECK_INTERVAL);
        destination = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void scheduleCheck(int delay) {
        nextCheck = golem.level().getGameTime() + delay;
    }

    private Vec3 homeCenter() {
        BlockPos home = golem.getHomePosition();
        return Vec3.atCenterOf(home);
    }
}
