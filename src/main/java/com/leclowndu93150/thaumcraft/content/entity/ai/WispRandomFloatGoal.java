package com.leclowndu93150.thaumcraft.content.entity.ai;

import com.leclowndu93150.thaumcraft.content.entity.WispEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;

public final class WispRandomFloatGoal extends Goal {
    private static final int HORIZONTAL_SPREAD = 7;
    private static final int VERTICAL_SPREAD = 6;
    private static final int VERTICAL_BIAS = 2;
    private static final int REPICK_ONE_IN = 30;
    private static final double ARRIVE_DISTANCE_SQ = 4.0;
    private static final int MAX_SURFACE_CLEARANCE = 8;
    private static final int PICK_ATTEMPTS = 10;

    private final WispEntity wisp;

    public WispRandomFloatGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        MoveControl moveControl = wisp.getMoveControl();
        if (!moveControl.hasWanted()) {
            return true;
        }
        double dx = moveControl.getWantedX() - wisp.getX();
        double dy = moveControl.getWantedY() - wisp.getY();
        double dz = moveControl.getWantedZ() - wisp.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq < ARRIVE_DISTANCE_SQ || wisp.getRandom().nextInt(reducedTickDelay(REPICK_ONE_IN)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        RandomSource random = wisp.getRandom();
        for (int attempt = 0; attempt < PICK_ATTEMPTS; attempt++) {
            BlockPos candidate = BlockPos.containing(
                    wisp.getX() + random.nextInt(HORIZONTAL_SPREAD) - random.nextInt(HORIZONTAL_SPREAD),
                    wisp.getY() + random.nextInt(VERTICAL_SPREAD) - VERTICAL_BIAS,
                    wisp.getZ() + random.nextInt(HORIZONTAL_SPREAD) - random.nextInt(HORIZONTAL_SPREAD));
            if (isValidWaypoint(candidate)) {
                wisp.getMoveControl().setWantedPosition(
                        candidate.getX() + 0.5, candidate.getY() + 0.1, candidate.getZ() + 0.5, 1.0);
                return;
            }
        }
    }

    private boolean isValidWaypoint(BlockPos pos) {
        if (!wisp.level().isEmptyBlock(pos)) {
            return false;
        }
        if (pos.getY() <= wisp.level().getMinY()) {
            return false;
        }
        int surface = wisp.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        return pos.getY() <= surface + MAX_SURFACE_CLEARANCE;
    }
}
