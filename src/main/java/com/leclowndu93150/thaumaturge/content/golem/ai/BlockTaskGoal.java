package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class BlockTaskGoal extends TaskGoal {
    private static final double BLOCK_REACH_SQR = 4.0D;
    private static final double PATH_END_TOLERANCE_SQR = 2.25D;
    private static final int VERTICAL_ACCESS = 2;
    private static final int REACH_RANGE = 0;
    private static final float LOOK_TURN_SPEED = 10.0F;

    public BlockTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
        this.reachSqr = BLOCK_REACH_SQR;
    }

    @Override
    protected boolean claim(ServerLevel level) {
        for (Task task : TaskBoard.of(level).openBlockTasks(golem.getUUID(), golem)) {
            if (mayTake(task, task.pos()) && reachable(task.pos())) {
                take(task);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void approach(Task task) {
        Vec3 spot = destination(task.pos());
        golem.getNavigation().moveTo(spot.x, spot.y, spot.z, golem.travelSpeed());
    }

    @Override
    protected double distanceSqrTo(Task task) {
        return golem.distanceToSqr(destination(task.pos()));
    }

    @Override
    public void tick() {
        Task job = golem.activeJob();
        if (job != null) {
            faceTarget(job.pos());
        }
        super.tick();
    }

    private void faceTarget(BlockPos pos) {
        Vec3 focus = Vec3.atCenterOf(pos);
        golem.getLookControl().setLookAt(focus.x, focus.y, focus.z, LOOK_TURN_SPEED, golem.getMaxHeadXRot());
    }

    private boolean reachable(BlockPos pos) {
        if (golem.distanceToSqr(Vec3.atCenterOf(pos)) <= reachSqr) {
            return true;
        }
        Path route = golem.getNavigation().createPath(pos, REACH_RANGE);
        if (route == null || route.getEndNode() == null) {
            return false;
        }
        BlockPos finish = route.getEndNode().asBlockPos();
        return pos.distSqr(finish) < PATH_END_TOLERANCE_SQR || isVerticalAccess(pos, finish);
    }

    private static boolean isVerticalAccess(BlockPos wanted, BlockPos finish) {
        return finish.getX() == wanted.getX() && finish.getZ() == wanted.getZ() && Math.abs(finish.getY() - wanted.getY()) == VERTICAL_ACCESS;
    }

    private Vec3 destination(BlockPos pos) {
        Level level = golem.level();
        Vec3 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(side);
            if (!level.getBlockState(neighbour).getCollisionShape(level, neighbour).isEmpty()) {
                continue;
            }
            Vec3 center = Vec3.atCenterOf(neighbour);
            double distance = golem.distanceToSqr(center);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = center;
            }
        }
        return best == null ? Vec3.atCenterOf(pos) : best;
    }
}
