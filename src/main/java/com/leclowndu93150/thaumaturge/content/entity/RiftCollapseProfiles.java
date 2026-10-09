package com.leclowndu93150.thaumaturge.content.entity;

import java.util.EnumMap;
import java.util.Map;

final class RiftCollapseProfiles {
    private static final float TAINT_SECONDS = 120.0F;
    private static final float WEAKNESS_SECONDS = 300.0F;
    private static final float WARP_POINTS = 25.0F;

    private static final Map<EntityFluxRift.Stability, RiftCollapseProfile> TABLE = buildTable();

    private RiftCollapseProfiles() {}

    static RiftCollapseProfile forTier(EntityFluxRift.Stability tier) {
        return TABLE.getOrDefault(tier, RiftCollapseProfile.NONE);
    }

    private static Map<EntityFluxRift.Stability, RiftCollapseProfile> buildTable() {
        Map<EntityFluxRift.Stability, RiftCollapseProfile> table = new EnumMap<>(EntityFluxRift.Stability.class);
        table.put(EntityFluxRift.Stability.VERY_STABLE, RiftCollapseProfile.NONE);
        table.put(EntityFluxRift.Stability.STABLE, new RiftCollapseProfile(0.0F, 0.0F, WARP_POINTS));
        table.put(EntityFluxRift.Stability.UNSTABLE, new RiftCollapseProfile(0.0F, WEAKNESS_SECONDS, WARP_POINTS));
        table.put(EntityFluxRift.Stability.VERY_UNSTABLE, new RiftCollapseProfile(TAINT_SECONDS, WEAKNESS_SECONDS, WARP_POINTS));
        return table;
    }
}
