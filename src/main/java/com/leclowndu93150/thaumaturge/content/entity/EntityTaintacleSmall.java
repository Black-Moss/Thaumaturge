package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EntityTaintacleSmall extends AbstractTaintacle {
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 2.0;
    private static final int LIFETIME_TICKS = 200;
    private static final float EXPIRY_DAMAGE = 10.0F;

    private int lifetime = LIFETIME_TICKS;

    public EntityTaintacleSmall(EntityType<? extends EntityTaintacleSmall> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTaintacleAttributes(MAX_HEALTH, ATTACK_DAMAGE);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel server && --this.lifetime <= 0) {
            this.hurtServer(server, server.damageSources().magic(), EXPIRY_DAMAGE);
        }
    }
}
