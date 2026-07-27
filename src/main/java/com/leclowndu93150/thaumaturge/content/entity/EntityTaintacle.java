package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EntityTaintacle extends AbstractTaintacle {
    public EntityTaintacle(EntityType<? extends EntityTaintacle> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTaintacleAttributes(50.0, 7.0);
    }
}
