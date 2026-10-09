package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

final class NodeOrbs {
    private static final int MINIMUM_SHARE_CAP = 10;
    private static final int SHARE_NUMERATOR = 2;
    private static final int SHARE_DENOMINATOR = 20;
    private static final int LAST_ORB_INDEX = 20;
    private static final double BLOCK_CENTER = 0.5;

    private NodeOrbs() {}

    static void burst(ServerLevel level, BlockPos pos, AspectList contained) {
        RandomSource random = level.getRandom();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : WandChargingEvents.reduceToPrimals(contained).entrySet()) {
            int total = entry.getValue();
            int maxShare = Math.max(MINIMUM_SHARE_CAP, (SHARE_NUMERATOR * total + SHARE_DENOMINATOR - 1) / SHARE_DENOMINATOR);
            int remaining = total;
            int made = 0;
            while (remaining > 0) {
                made++;
                int share = made >= LAST_ORB_INDEX ? remaining : Math.min(remaining, 1 + random.nextInt(maxShare));
                level.addFreshEntity(new EntityAspectOrb(level, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, entry.getKey(), share));
                remaining -= share;
            }
        }
    }
}
