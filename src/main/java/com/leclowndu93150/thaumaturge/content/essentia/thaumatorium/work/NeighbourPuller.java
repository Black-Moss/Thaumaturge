package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;

public final class NeighbourPuller {
    public static final int PULL_SUCTION = 128;

    private static final int PULL_UNITS = 1;
    private static final int NOTHING_GAINED = 0;

    private final NeighbourRing ring = new NeighbourRing();

    public int pull(ServerLevel level, BlockPos origin, Direction front, Holder<IAspect> aspect) {
        for (int slot = 0; slot < ring.slots(); slot++) {
            if (!ring.aim(origin, front, slot) || !level.hasChunkAt(ring.pos())) {
                continue;
            }
            Direction face = ring.side().getOpposite();
            IEssentiaTransport peer = level.getCapability(EssentiaCapabilities.TRANSPORT, ring.pos(), face);
            if (peer == null || peer.getEssentiaAmount(face) < PULL_UNITS || !peer.canOutputTo(face) || peer.getSuctionAmount(face) >= PULL_SUCTION || PULL_SUCTION < peer.getMinimumSuction()) {
                continue;
            }
            int gained = peer.takeEssentia(aspect, PULL_UNITS, face);
            if (gained > NOTHING_GAINED) {
                return gained;
            }
        }
        return NOTHING_GAINED;
    }
}
