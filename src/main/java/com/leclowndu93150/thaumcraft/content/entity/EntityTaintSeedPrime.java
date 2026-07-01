package com.leclowndu93150.thaumcraft.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EntityTaintSeedPrime extends AbstractTaintSeed {
    public EntityTaintSeedPrime(EntityType<? extends EntityTaintSeedPrime> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSeedAttributes(150.0, 7.0);
    }

    @Override
    public int getArea() {
        return 2;
    }
}
