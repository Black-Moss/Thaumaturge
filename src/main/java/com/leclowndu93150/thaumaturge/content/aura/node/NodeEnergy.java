package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class NodeEnergy {
    private static final int REFILL_INTERVAL = 20;
    private static final int POINT_DIVISOR = 20;
    private static final float TOLERANCE = 0.01F;

    private NodeEnergy() {}

    static boolean refillDue(ServerLevel level) {
        return level.getGameTime() % REFILL_INTERVAL == 0;
    }

    static boolean refill(BlockEntityNode node, ServerLevel level, BlockPos pos) {
        float cost = ThaumaturgeCommonConfig.ENERGIZED_NODE_VIS_PER_POINT.get().floatValue();
        boolean flux = node.kind() == NodeType.TAINTED;
        boolean changed = false;
        List<AspectInstance> base = node.aspectsBase.entries();
        for (int i = 0; i < base.size(); i++) {
            AspectInstance entry = base.get(i);
            int attempts = Math.max(1, entry.amount() / POINT_DIVISOR);
            for (int attempt = 0; attempt < attempts && node.held.amountOf(entry.aspect()) < entry.amount(); attempt++) {
                float taken = flux ? AuraHelper.drainFlux(level, pos, cost, false) : AuraHelper.drainVis(level, pos, cost, false);
                if (taken >= cost - TOLERANCE) {
                    node.held = node.held.add(entry.aspect(), 1);
                    changed = true;
                    continue;
                }
                if (taken > 0.0F) {
                    if (flux) {
                        AuraHelper.addFlux(level, pos, taken);
                    } else {
                        AuraHelper.addVis(level, pos, taken);
                    }
                }
                break;
            }
        }
        return changed;
    }
}
