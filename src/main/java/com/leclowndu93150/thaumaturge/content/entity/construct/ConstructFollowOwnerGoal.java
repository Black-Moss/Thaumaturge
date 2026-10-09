package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public final class ConstructFollowOwnerGoal extends Goal {
    private static final int REPATH_INTERVAL = 10;
    private static final float LOOK_YAW_SPEED = 10.0F;
    private static final double TELEPORT_DISTANCE_SQR = 144.0;
    private static final int RING_RADIUS = 2;
    private static final float WATER_COST = 0.0F;
    private static final double COLUMN_CENTER = 0.5;

    private final EntityOwnedConstruct construct;
    private final PathNavigation navigation;
    private final double speed;
    private final float startDistanceSqr;
    private final float stopDistanceSqr;
    private float previousWaterCost;
    private int repathTimer;
    private @Nullable LivingEntity owner;

    public ConstructFollowOwnerGoal(EntityOwnedConstruct construct, double speed, float startDistance, float stopDistance) {
        this.construct = construct;
        this.navigation = construct.getNavigation();
        this.speed = speed;
        this.startDistanceSqr = startDistance * startDistance;
        this.stopDistanceSqr = stopDistance * stopDistance;
        setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        owner = null;
        LivingEntity found = construct.getOwner();
        if (found != null && construct.distanceToSqr(found) >= startDistanceSqr) {
            owner = found;
        }
        return owner != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || navigation.isDone()) {
            return false;
        }
        return construct.distanceToSqr(owner) > stopDistanceSqr;
    }

    @Override
    public void start() {
        repathTimer = 0;
        previousWaterCost = construct.getPathfindingMalus(PathType.WATER);
        construct.setPathfindingMalus(PathType.WATER, WATER_COST);
    }

    @Override
    public void stop() {
        owner = null;
        navigation.stop();
        construct.setPathfindingMalus(PathType.WATER, previousWaterCost);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = owner;
        if (target == null) {
            return;
        }
        construct.getLookControl().setLookAt(target, LOOK_YAW_SPEED, construct.getMaxHeadXRot());
        repathTimer--;
        if (repathTimer <= 0) {
            repathTimer = adjustedTickDelay(REPATH_INTERVAL);
            chase(target);
        }
    }

    private void chase(LivingEntity target) {
        boolean pathed = navigation.moveTo(target, speed);
        if (pathed || construct.isLeashed()) {
            return;
        }
        if (construct.distanceToSqr(target) >= TELEPORT_DISTANCE_SQR) {
            teleportBeside(target);
        }
    }

    private void teleportBeside(LivingEntity target) {
        Level level = construct.level();
        BlockPos origin = target.blockPosition();
        BlockPos.MutableBlockPos spot = new BlockPos.MutableBlockPos();
        for (int dx = -RING_RADIUS; dx <= RING_RADIUS; dx++) {
            int dzStep = Math.abs(dx) == RING_RADIUS ? 1 : 2 * RING_RADIUS;
            for (int dz = -RING_RADIUS; dz <= RING_RADIUS; dz += dzStep) {
                spot.set(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                if (!isSafeColumn(level, spot)) {
                    continue;
                }
                construct.snapTo(spot.getX() + COLUMN_CENTER, spot.getY(), spot.getZ() + COLUMN_CENTER, construct.getYRot(), construct.getXRot());
                navigation.stop();
                return;
            }
        }
    }

    private static boolean isFullSolid(Level level, BlockPos pos) {
        return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isSafeColumn(Level level, BlockPos.MutableBlockPos column) {
        return level.hasChunkAt(column) && isFullSolid(level, column.below()) && !isFullSolid(level, column) && !isFullSolid(level, column.above());
    }
}
