package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class PullSelector {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MIN_UNITS = 1;

    private final BufferSides sides;
    private final BufferSuctionPolicy policy;

    public PullSelector(BufferSides sides, BufferSuctionPolicy policy) {
        this.sides = sides;
        this.policy = policy;
    }

    public @Nullable PullCandidate select(Level level, BlockPos pos, int firstSideOrdinal) {
        for (int i = firstSideOrdinal; i < DIRECTIONS.length; i++) {
            Direction side = DIRECTIONS[i];
            if (!sides.isOpen(side)) {
                continue;
            }
            Direction back = side.getOpposite();
            IEssentiaConduit peer = EssentiaFlowHandler.conduit(level, pos.relative(side), back);
            if (peer == null || !peer.connectsOn(back)) {
                continue;
            }
            Holder<IAspect> offered = peer.heldAspect(back);
            int offer = policy.offered(sides.choke(side));
            if (offered != null && peer.heldAmount(back) >= MIN_UNITS && peer.releasesOn(back) && peer.pull(back).strength() < offer && offer >= peer.pullThreshold()) {
                return new PullCandidate(side, peer, offered);
            }
        }
        return null;
    }
}
