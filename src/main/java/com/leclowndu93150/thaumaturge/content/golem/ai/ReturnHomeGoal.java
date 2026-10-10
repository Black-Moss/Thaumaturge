package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ReturnHomeGoal extends Goal {
    private static final int FIRST_CHECK_DELAY = 10;
    private static final int CHECK_INTERVAL = 50;
    private static final double AT_HOME_DISTANCE = 2.2D;
    private static final double SETTLED_DISTANCE = 1.7D;
    private static final double STRAIGHT_WALK_LIMIT = 32.0D;
    private static final int WAYPOINT_HORIZONTAL = 16;
    private static final int WAYPOINT_VERTICAL = 7;
    private static final double WAYPOINT_ARC = Math.PI / 2.0D;

    private final EntityThaumaturgeGolem golem;
    private long nextCheck;
    private @Nullable Vec3 destination;

    public ReturnHomeGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        this.nextCheck = golem.level().getGameTime() + FIRST_CHECK_DELAY;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        long now = golem.level().getGameTime();
        if (now < nextCheck) {
            return false;
        }
        nextCheck = now + CHECK_INTERVAL;
        if (!golem.hasHome() || holdsTask() || !golem.getNavigation().isDone()) {
            return false;
        }
        destination = chooseDestination();
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
        return golem.hasHome() && !holdsTask() && !golem.getNavigation().isDone() && golem.distanceToSqr(homePoint()) > SETTLED_DISTANCE * SETTLED_DISTANCE;
    }

    @Override
    public void stop() {
        golem.getNavigation().stop();
        destination = null;
        nextCheck = golem.level().getGameTime() + CHECK_INTERVAL;
    }

    private @Nullable Vec3 chooseDestination() {
        Vec3 home = homePoint();
        double distanceSqr = golem.distanceToSqr(home);
        if (distanceSqr <= AT_HOME_DISTANCE * AT_HOME_DISTANCE) {
            return null;
        }
        if (distanceSqr > STRAIGHT_WALK_LIMIT * STRAIGHT_WALK_LIMIT) {
            return DefaultRandomPos.getPosTowards(golem, WAYPOINT_HORIZONTAL, WAYPOINT_VERTICAL, home, WAYPOINT_ARC);
        }
        return home;
    }

    private Vec3 homePoint() {
        return Vec3.atCenterOf(golem.getHomePosition());
    }

    private boolean holdsTask() {
        Task held = golem.activeJob();
        return held != null && !held.isEnded();
    }
}
