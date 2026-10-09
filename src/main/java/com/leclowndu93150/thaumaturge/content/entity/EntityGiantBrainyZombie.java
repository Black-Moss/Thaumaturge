package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.level.Level;

public final class EntityGiantBrainyZombie extends EntityBrainyZombie {
    private static final EntityDataAccessor<Float> ANGER = SynchedEntityData.defineId(EntityGiantBrainyZombie.class, EntityDataSerializers.FLOAT);
    private static final double MAX_HEALTH = 60.0;
    private static final double BASE_ATTACK_DAMAGE = 7.0;
    private static final int EXPERIENCE_REWARD = 15;
    private static final int LEAP_PRIORITY = 2;
    private static final float LEAP_STRENGTH = 0.4F;
    private static final float MAX_ANGER = 2.0F;
    private static final float ANGER_PER_HIT = 0.1F;
    private static final float ANGER_DECAY_THRESHOLD = 1.0F;
    private static final float ANGER_DECAY = 0.002F;
    private static final double SCALE_BASE = 1.0;
    private static final double ATTACK_PER_ANGER = 5.0;

    public EntityGiantBrainyZombie(EntityType<? extends EntityGiantBrainyZombie> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = EntityBrainyZombie.createAttributes();
        builder.add(Attributes.MAX_HEALTH, MAX_HEALTH);
        builder.add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE);
        return builder;
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(LEAP_PRIORITY, new LeapAtTargetGoal(this, LEAP_STRENGTH));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER, 0.0F);
    }

    public void setAnger(float anger) {
        this.entityData.set(ANGER, Math.max(0.0F, Math.min(anger, MAX_ANGER)));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.setAnger(this.getAnger() + ANGER_PER_HIT);
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            coolDown();
            resizeForAnger();
        }
    }

    public float getAnger() {
        return this.entityData.get(ANGER).floatValue();
    }

    private void coolDown() {
        float current = this.getAnger();
        if (current > ANGER_DECAY_THRESHOLD) {
            this.setAnger(current - ANGER_DECAY);
        }
    }

    private void resizeForAnger() {
        float anger = this.getAnger();
        double excess = anger - ANGER_DECAY_THRESHOLD;
        setBase(Attributes.ATTACK_DAMAGE, excess * ATTACK_PER_ANGER + BASE_ATTACK_DAMAGE);
        setBase(Attributes.SCALE, anger + SCALE_BASE);
    }

    private void setBase(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        instance.setBaseValue(value);
    }
}
