package com.leclowndu93150.thaumcraft.content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EntityTaintacleSmall extends AbstractTaintacle {
    private static final int MAX_LIFETIME = 200;
    private static final float SELF_DAMAGE = 10.0F;

    private int lifetime;

    public EntityTaintacleSmall(EntityType<? extends EntityTaintacleSmall> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.lifetime = MAX_LIFETIME;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTaintacleAttributes(5.0, 2.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel server) {
            lifetime--;
            if (lifetime <= 0) {
                this.hurtServer(server, server.damageSources().magic(), SELF_DAMAGE);
            }
        }
    }
}
