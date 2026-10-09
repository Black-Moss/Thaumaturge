package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntityInhabitedZombie extends Zombie {
    private static final Map<EquipmentSlot, Float> NO_DROP_OVERRIDES = Map.of();
    private static final double MAX_HEALTH = 30.0;
    private static final double ATTACK_DAMAGE = 5.0;
    private static final double REINFORCEMENT_CHANCE = 0.0;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int CULTIST_PRIORITY = 3;
    private static final double SPAWN_EXCLUSION_HORIZONTAL = 32.0;
    private static final double SPAWN_EXCLUSION_VERTICAL = 16.0;
    private static final int DEATH_PARTICLES = 20;
    private static final double DEATH_PARTICLE_HORIZONTAL_SPREAD = 0.5;
    private static final double DEATH_PARTICLE_VERTICAL_SPREAD = 0.25;
    private static final double DEATH_PARTICLE_SPEED = 0.02;
    private static final double HALF = 0.5;

    public EntityInhabitedZombie(EntityType<? extends EntityInhabitedZombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, REINFORCEMENT_CHANCE);
    }

    public static boolean checkInhabitedSpawnRules(EntityType<EntityInhabitedZombie> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        AABB exclusion = new AABB(pos).inflate(SPAWN_EXCLUSION_HORIZONTAL, SPAWN_EXCLUSION_VERTICAL, SPAWN_EXCLUSION_HORIZONTAL);
        return level.getEntitiesOfClass(EntityInhabitedZombie.class, exclusion).isEmpty() && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        NearestAttackableTargetGoal<EntityCultist> huntCultists = new NearestAttackableTargetGoal<>(this, EntityCultist.class, true);
        HurtByTargetGoal retaliate = new HurtByTargetGoal(this);
        this.targetSelector.addGoal(CULTIST_PRIORITY, huntCultists);
        this.targetSelector.addGoal(RETALIATE_PRIORITY, retaliate);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity, DamageSource source) {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.equip(TTLootTables.EQUIPMENT_INHABITED_ZOMBIE, NO_DROP_OVERRIDES);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.CRABTALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected void tickDeath() {
        if (!(this.level() instanceof ServerLevel server)) {
            super.tickDeath();
            return;
        }
        if (this.isRemoved()) {
            return;
        }
        releaseCrab(server);
        awardLeftoverExperience(server);
        puffOfSmoke(server);
        this.discard();
    }

    private void releaseCrab(ServerLevel server) {
        EntityEldritchCrab crab = TTEntities.ELDRITCH_CRAB.get().create(server, EntitySpawnReason.TRIGGERED);
        if (crab == null) {
            return;
        }
        crab.snapTo(this.getX(), this.getY() + this.getEyeHeight(), this.getZ(), this.getYRot(), this.getXRot());
        crab.setHelm(true);
        server.addFreshEntity(crab);
    }

    private void awardLeftoverExperience(ServerLevel server) {
        boolean unclaimed = this.lastHurtByPlayerMemoryTime <= 0 && !this.wasExperienceConsumed();
        if (unclaimed && this.shouldDropExperience() && server.getGameRules().get(GameRules.MOB_DROPS)) {
            ExperienceOrb.award(server, this.position(), this.getExperienceReward(server, null));
        }
    }

    private void puffOfSmoke(ServerLevel server) {
        double width = this.getBbWidth();
        double height = this.getBbHeight();
        double spreadX = width * DEATH_PARTICLE_HORIZONTAL_SPREAD;
        double spreadY = height * DEATH_PARTICLE_VERTICAL_SPREAD;
        double centerY = this.getY() + height * HALF;
        server.sendParticles(ParticleTypes.POOF, this.getX(), centerY, this.getZ(), DEATH_PARTICLES, spreadX, spreadY, spreadX, DEATH_PARTICLE_SPEED);
    }
}
