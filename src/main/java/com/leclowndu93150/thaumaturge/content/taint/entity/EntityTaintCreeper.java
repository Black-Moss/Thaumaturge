package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public final class EntityTaintCreeper extends Creeper implements ITaintedMob {
    private static final double MAX_HEALTH = 24.0;
    private static final double MOVEMENT_SPEED = 0.28;
    private static final double FOLLOW_RANGE = 24.0;

    public EntityTaintCreeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }
}
