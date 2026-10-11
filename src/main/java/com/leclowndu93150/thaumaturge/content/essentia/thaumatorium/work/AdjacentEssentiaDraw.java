package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;

public final class AdjacentEssentiaDraw {
    public static final int DRAW_SUCTION = 128;

    private static final Direction[] SIDES = Direction.values();
    private static final int ONE_POINT = 1;
    private static final int NOTHING = 0;

    private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

    public int drawOne(ServerLevel level, BlockPos lower, Direction front, Holder<IAspect> wanted) {
        for (Direction side : SIDES) {
            if (side != front && side != Direction.DOWN && side != Direction.UP && takeFrom(level, lower, side, wanted)) {
                return ONE_POINT;
            }
        }
        BlockPos upper = lower.above();
        for (Direction side : SIDES) {
            if (side != front && side != Direction.DOWN && takeFrom(level, upper, side, wanted)) {
                return ONE_POINT;
            }
        }
        return NOTHING;
    }

    private boolean takeFrom(ServerLevel level, BlockPos body, Direction side, Holder<IAspect> wanted) {
        probe.setWithOffset(body, side);
        Direction touching = side.getOpposite();
        IEssentiaConduit neighbour = EssentiaFlowHandler.conduit(level, probe, touching);
        if (neighbour == null || !willGive(neighbour, touching)) {
            return false;
        }
        return neighbour.extract(wanted, ONE_POINT, touching, false) == ONE_POINT;
    }

    private static boolean willGive(IEssentiaConduit neighbour, Direction touching) {
        return neighbour.releasesOn(touching) && neighbour.heldAmount(touching) >= ONE_POINT && neighbour.pull(touching).strength() < DRAW_SUCTION && neighbour.pullThreshold() <= DRAW_SUCTION;
    }
}
