package com.leclowndu93150.thaumaturge.content.essentia.tube.vent;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeFilter;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class PressureClash {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int NO_SUCTION = 0;
    private static final int SUCTION_DROP = 1;

    private PressureClash() {}

    public static ClashVerdict assess(Level level, BlockPos pos, IEssentiaConduit own, int suction, @Nullable Holder<IAspect> ownAspect) {
        if (suction <= NO_SUCTION) {
            return ClashVerdict.CLEAR;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction side : DIRECTIONS) {
            if (!own.connectsOn(side)) {
                continue;
            }
            Direction back = side.getOpposite();
            cursor.setWithOffset(pos, side);
            IEssentiaConduit peer = EssentiaFlowHandler.conduit(level, cursor, back);
            if (peer == null || !peer.connectsOn(back) || level.getBlockEntity(cursor) instanceof BlockEntityTubeFilter) {
                continue;
            }
            EssentiaPull peerPull = peer.pull(back);
            if (isOpposed(suction, peerPull.strength()) && !Objects.equals(ownAspect, peerPull.aspect())) {
                return ClashVerdict.CLASH;
            }
        }
        return ClashVerdict.CLEAR;
    }

    private static boolean isOpposed(int suction, int peerSuction) {
        return peerSuction > NO_SUCTION && (peerSuction == suction || peerSuction == suction - SUCTION_DROP);
    }
}
