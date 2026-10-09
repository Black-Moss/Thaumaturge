package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public final class EntityTaskGoal extends TaskGoal {
    private static final double BASE_REACH_SQR = 3.5D;
    private static final int REACH_RANGE = 0;
    private static final float LOOK_TURN_SPEED = 10.0F;
    private static final float HALF = 0.5F;

    public EntityTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    protected boolean claim(ServerLevel level) {
        for (Task task : TaskBoard.of(level).openEntityTasks(golem.getUUID(), golem)) {
            Entity target = task.entity();
            if (target == null || !mayTake(task, target.blockPosition())) {
                continue;
            }
            retarget(task);
            if (reachable(target)) {
                take(task);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void approach(Task task) {
        Entity target = task.entity();
        if (target != null) {
            golem.getNavigation().moveTo(target, golem.travelSpeed());
        }
    }

    @Override
    protected double distanceSqrTo(Task task) {
        Entity target = task.entity();
        return target == null ? Double.POSITIVE_INFINITY : golem.distanceToSqr(target);
    }

    @Override
    boolean adopts(Task task) {
        return task.isEntityTask();
    }

    @Override
    void retarget(Task task) {
        Entity target = task.entity();
        if (target != null) {
            reachSqr = BASE_REACH_SQR + Mth.square(target.getBbWidth() * HALF);
        }
    }

    @Override
    public void tick() {
        Task task = golem.activeJob();
        Entity target = task == null ? null : task.entity();
        if (target != null) {
            golem.getLookControl().setLookAt(target, LOOK_TURN_SPEED, golem.getMaxHeadXRot());
        }
        super.tick();
    }

    private boolean reachable(Entity target) {
        if (golem.distanceToSqr(target) <= reachSqr) {
            return true;
        }
        Path path = golem.getNavigation().createPath(target, REACH_RANGE);
        Node end = path == null ? null : path.getEndNode();
        if (end == null) {
            return false;
        }
        double dx = end.x - target.getBlockX();
        double dz = end.z - target.getBlockZ();
        return dx * dx + dz * dz < reachSqr;
    }
}
