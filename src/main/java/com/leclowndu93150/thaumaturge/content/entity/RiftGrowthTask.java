package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class RiftGrowthTask implements RiftTask {
    private static final int GROWTH_INTERVAL = 600;
    private static final double COST_SIZE_FACTOR = 2.0;

    @Override
    public int interval() {
        return GROWTH_INTERVAL;
    }

    @Override
    public void run(ServerLevel level, EntityFluxRift rift) {
        int size = rift.currentSize();
        if (size >= EntityFluxRift.MAX_RIFT_SIZE || rift.stabilityTier() == EntityFluxRift.Stability.VERY_STABLE) {
            return;
        }
        float cost = (float) Math.sqrt(COST_SIZE_FACTOR * size);
        BlockPos pos = rift.blockPosition();
        if (AuraHelper.getFlux(level, pos) < cost) {
            return;
        }
        AuraHelper.drainFlux(level, pos, cost, false);
        rift.resize(size + 1);
    }
}
