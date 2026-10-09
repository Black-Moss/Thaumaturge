package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class EntityMindSpider extends Spider {
    private static final EntityDataAccessor<Boolean> HARMLESS = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> VIEWER = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.STRING);
    private static final String HARMLESS_KEY = "is_hallucination";
    private static final String VIEWER_KEY = "observer_name";
    private static final String LEGACY_HARMLESS_KEY = "harmless";
    private static final String LEGACY_VIEWER_KEY = "viewer";
    private static final double MAX_HEALTH = 1.0;
    private static final double ATTACK_DAMAGE = 1.0;
    private static final int EXPERIENCE_REWARD = 1;
    private static final int HALLUCINATION_LIFESPAN = 1200;
    private static final float VOICE_PITCH = 0.7F;

    public EntityMindSpider(EntityType<? extends EntityMindSpider> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HARMLESS, false);
        builder.define(VIEWER, "");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(HARMLESS_KEY, this.isIllusion());
        output.putString(VIEWER_KEY, this.witnessName());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(HARMLESS, input.getBooleanOr(HARMLESS_KEY, input.getBooleanOr(LEGACY_HARMLESS_KEY, false)));
        this.entityData.set(VIEWER, input.getStringOr(VIEWER_KEY, input.getStringOr(LEGACY_VIEWER_KEY, "")));
    }

    public String witnessName() {
        return this.entityData.get(VIEWER);
    }

    public boolean isIllusion() {
        return this.entityData.get(HARMLESS);
    }

    public void bindIllusion(String witness) {
        this.entityData.set(VIEWER, witness);
        this.entityData.set(HARMLESS, true);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || !this.isIllusion()) {
            return;
        }
        if (this.tickCount > HALLUCINATION_LIFESPAN) {
            this.discard();
        }
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public float getVoicePitch() {
        return VOICE_PITCH;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return !this.isIllusion() && super.doHurtTarget(level, target);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return this.isIllusion() ? 0 : super.getBaseExperienceReward(level);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        List.copyOf(this.getPassengers()).forEach(EntityMindSpider::banishRider);
        return data;
    }

    private static void banishRider(Entity rider) {
        rider.stopRiding();
        rider.discard();
    }
}
