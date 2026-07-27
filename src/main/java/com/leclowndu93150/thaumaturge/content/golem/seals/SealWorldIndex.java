package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SealWorldIndex {
    private final Map<SealPos, SealEntity> seals = new ConcurrentHashMap<>();

    public Map<SealPos, SealEntity> seals() {
        return seals;
    }
}
