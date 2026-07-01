package com.leclowndu93150.thaumcraft.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EntityTaintSeed extends AbstractTaintSeed {
    public EntityTaintSeed(EntityType<? extends EntityTaintSeed> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSeedAttributes(75.0, 4.0);
    }

    @Override
    public int getArea() {
        return 1;
    }
}
