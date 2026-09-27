package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;

public final class EntityTaintSpider extends Spider implements ITaintedMob {
    private static final int XP_REWARD = 2;
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 2.0;
    private static final double FOLLOW_RANGE = 12.0;

    public EntityTaintSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }
}
