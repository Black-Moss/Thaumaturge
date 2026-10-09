package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TakeArbitration {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int NO_SUCTION = 0;

    private final BufferSides sides;
    private final BufferSuctionPolicy policy;

    public TakeArbitration(BufferSides sides, BufferSuctionPolicy policy) {
        this.sides = sides;
        this.policy = policy;
    }

    public TakeVerdict judge(Level level, BlockPos pos, Holder<IAspect> aspect, Direction asking) {
        int askerSuction = peerSuction(level, pos, asking, null);
        for (Direction side : DIRECTIONS) {
            if (side == asking || !sides.isOpen(side)) {
                continue;
            }
            int rival = peerSuction(level, pos, side, aspect);
            if (rival > askerSuction && rival > policy.offered(sides.choke(side))) {
                return TakeVerdict.OUTBID;
            }
        }
        return TakeVerdict.GRANTED;
    }

    private static int peerSuction(Level level, BlockPos pos, Direction side, @Nullable Holder<IAspect> accepted) {
        Direction back = side.getOpposite();
        IEssentiaTransport peer = EssentiaFlowHandler.transport(level, pos.relative(side), back);
        if (peer == null) {
            return NO_SUCTION;
        }
        Holder<IAspect> wanted = peer.getSuctionType(back);
        if (accepted != null && wanted != null && !wanted.equals(accepted)) {
            return NO_SUCTION;
        }
        return peer.getSuctionAmount(back);
    }
}
