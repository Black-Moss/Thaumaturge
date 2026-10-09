package com.leclowndu93150.thaumaturge.client.particle.support;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class TargetEntityResolver {
    private final int entityId;
    private @Nullable Entity resolved;

    public TargetEntityResolver(int entityId) {
        this.entityId = entityId;
    }

    public @Nullable Entity resolve(ClientLevel level) {
        if (this.resolved == null) {
            this.resolved = level.getEntity(this.entityId);
        }
        return this.resolved;
    }
}
