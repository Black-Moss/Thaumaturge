package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class EntityCultist extends Monster {
    public static final byte ARRIVAL_EVENT = 20;

    public record LookAngles(float yaw, float pitch) {
        public static LookAngles between(Vec3 from, Vec3 to) {
            double dx = to.x - from.x;
            double dy = to.y - from.y;
            double dz = to.z - from.z;
            double flat = Math.sqrt(dx * dx + dz * dz);
            float pitch = (float) -(Mth.atan2(dy, flat) * Mth.RAD_TO_DEG);
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - LOOK_YAW_OFFSET;
            return new LookAngles(yaw, pitch);
        }
    }

    public static final class Aiming {
        private Aiming() {}

        public static Vec3 launchVector(Vec3 origin, Vec3 aimPoint, Vec3 targetMotion, double leadTicks, double offsetX, double offsetZ) {
            Vec3 lead = aimPoint.add(targetMotion.scale(leadTicks));
            Vec3 delta = lead.subtract(origin);
            return new Vec3(delta.x + offsetX, delta.y, delta.z + offsetZ).normalize();
        }

        public static Vec3 launchVector(Vec3 origin, Vec3 aimPoint, Vec3 targetMotion, double leadTicks) {
            return launchVector(origin, aimPoint, targetMotion, leadTicks, 0.0, 0.0);
        }
    }

    static final int DOOR_PRIORITY = 4;
    static final int RESTRICTION_PRIORITY = 5;
    static final int STROLL_PRIORITY = 6;
    static final int LOOK_PLAYER_PRIORITY = 7;
    static final int LOOK_AROUND_PRIORITY = 8;

    private static final double FOLLOW_RANGE = 32.0;
    private static final double MOVEMENT_SPEED = 0.3;
    private static final double ATTACK_DAMAGE = 4.0;
    private static final int EXPERIENCE = 10;
    private static final float ARMOR_DROP_CHANCE = 0.05F;
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HEAD, EquipmentSlot.LEGS};
    private static final Map<EquipmentSlot, Float> NO_DROP_OVERRIDES = Map.of();
    private static final boolean LOOT_PICKUP_ENABLED = false;
    private static final double WALK_SPEED = 0.8;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final int GUARDIAN_PRIORITY = 3;
    private static final int ILLAGER_PRIORITY = 4;
    private static final int ARRIVAL_PUFFS = 20;
    private static final double ARRIVAL_DRIFT = 0.05;
    private static final float LOOK_YAW_OFFSET = 90.0F;

    protected EntityCultist(EntityType<? extends EntityCultist> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            this.setDropChance(slot, ARMOR_DROP_CHANCE);
        }
    }

    public static AttributeSupplier.Builder createCultistAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    protected static AttributeSupplier.Builder createCultistAttributes(double maxHealth) {
        return createCultistAttributes().add(Attributes.MAX_HEALTH, maxHealth);
    }

    protected abstract ResourceKey<LootTable> equipmentTable();

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.equip(this.equipmentTable(), NO_DROP_OVERRIDES);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {}

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return EntityCultist.class.isInstance(entity) || super.considersEntityAsAlly(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) && !(target instanceof EntityCultist);
    }

    @Override
    public boolean canPickUpLoot() {
        return LOOT_PICKUP_ENABLED;
    }

    public void lookAt(Vec3 focus, float headStep, float pitchStep) {
        LookAngles angles = LookAngles.between(new Vec3(this.getX(), this.getEyeY(), this.getZ()), focus);
        this.setYHeadRot(Mth.approachDegrees(this.getYHeadRot(), angles.yaw(), headStep));
        this.setXRot(Mth.approachDegrees(this.getXRot(), angles.pitch(), pitchStep));
    }

    public void spawnCultistArrivalParticles() {
        if (this.level() instanceof ServerLevel server) {
            server.broadcastEntityEvent(this, ARRIVAL_EVENT);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != ARRIVAL_EVENT) {
            super.handleEntityEvent(id);
            return;
        }
        RandomSource random = this.getRandom();
        for (int i = 0; i < ARRIVAL_PUFFS; i++) {
            this.level().addParticle(ParticleTypes.POOF, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), random.nextGaussian() * ARRIVAL_DRIFT, random.nextGaussian() * ARRIVAL_DRIFT,
                    random.nextGaussian() * ARRIVAL_DRIFT);
        }
    }

    final void addIdleGoals() {
        this.getNavigation().setCanOpenDoors(true);
        this.goalSelector.addGoal(DOOR_PRIORITY, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(RESTRICTION_PRIORITY, new MoveTowardsRestrictionGoal(this, WALK_SPEED));
        this.goalSelector.addGoal(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(this, WALK_SPEED));
        this.goalSelector.addGoal(LOOK_PLAYER_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        this.goalSelector.addGoal(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(this));
    }

    final void addTargetGoals() {
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new CultistHurtByTargetGoal(this));
        this.targetSelector.addGoal(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(GUARDIAN_PRIORITY, new NearestAttackableTargetGoal<>(this, EntityEldritchGuardian.class, true));
        this.targetSelector.addGoal(ILLAGER_PRIORITY, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }
}
