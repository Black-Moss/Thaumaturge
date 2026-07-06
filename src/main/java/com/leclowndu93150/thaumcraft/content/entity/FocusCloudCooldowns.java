package com.leclowndu93150.thaumcraft.content.entity;

import java.util.HashMap;
import java.util.Map;

public final class FocusCloudCooldowns {
    private final Map<Integer, Long> entities = new HashMap<>();
    private final Map<Long, Long> blocks = new HashMap<>();

    public boolean tryClaimEntity(int entityId, long now, long duration) {
        Long until = entities.get(entityId);
        if (until != null && until > now) {
            return false;
        }
        entities.put(entityId, now + duration);
        return true;
    }

    public boolean tryClaimBlock(long packedPos, long now, long duration) {
        Long until = blocks.get(packedPos);
        if (until != null && until > now) {
            return false;
        }
        blocks.put(packedPos, now + duration);
        return true;
    }
}
