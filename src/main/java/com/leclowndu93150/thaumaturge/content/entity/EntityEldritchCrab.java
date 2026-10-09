package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchCrab extends Monster {
    private static final EntityDataAccessor<Boolean> HELM = SynchedEntityData.defineId(EntityEldritchCrab.class, EntityDataSerializers.BOOLEAN);
    private static final List<Holder<MobEffect>> SPAWN_BOONS = List.of(MobEffects.SPEED, MobEffects.STRENGTH, MobEffects.REGENERATION, MobEffects.INVISIBILITY);
    private static final String HELM_KEY = "helm";
    private static final double MAX_HEALTH = 20.0;
    private static final double ATTACK_DAMAGE = 4.0;
    private static final double BARE_SPEED = 0.3;
    private static final double HELM_SPEED = 0.275;
    private static final double HELM_ARMOR = 5.0;
    private static final double BARE_ARMOR = 0.0;
    private static final int EXPERIENCE = 6;
    private static final float LEAP_STRENGTH = 0.63F;
    private static final double MELEE_SPEED = 1.0;
    private static final double STROLL_SPEED = 0.8;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final int LEAP_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int STROLL_PRIORITY = 5;
    private static final int LOOK_PLAYER_PRIORITY = 6;
    private static final int LOOK_AROUND_PRIORITY = 7;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final int CULTIST_PRIORITY = 3;
    private static final float STEP_VOLUME = 0.15F;
    private static final float STEP_PITCH = 1.0F;
    private static final int AMBIENT_INTERVAL = 160;
    private static final float HELM_CHANCE = 0.33F;
    private static final float BOON_CHANCE = 0.1F;
    private static final double LATCH_SQUARED_DISTANCE = 4.0;
    private static final int MOUNTED_ATTACK_BASE = 10;
    private static final int MOUNTED_ATTACK_SPREAD = 10;
    private static final float DISMOUNT_CHANCE = 0.2F;
    private static final int FALL_GRACE_TICKS = 20;
    private static final float CLAW_VOLUME = 1.0F;
    private static final float CLAW_PITCH_BASE = 0.9F;
    private static final float CLAW_PITCH_SPREAD = 0.2F;
    private static final float HELM_BREAK_HEALTH = 0.5F;
    private static final int HELM_BREAK_PARTICLES = 8;
    private static final double HELM_BREAK_SPREAD = 0.1;
    private static final double HELM_BREAK_SPEED = 0.05;
    private static final int SHELL_BREAK_TICKS = 8;
    private static final double SEAT_WIDTH_FACTOR = 0.5;
    private static final double SEAT_CRAB_WIDTH_FACTOR = 0.25;
    private static final float FACE_VICTIM_TURN = 180.0F;

    private int attackTimer;
    private int shellBreakTicks;
    private boolean shownHelm;

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, BARE_SPEED);
    }

    public EntityEldritchCrab(EntityType<? extends EntityEldritchCrab> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.CRABDEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, STEP_VOLUME, STEP_PITCH);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.CRABTALK.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return AMBIENT_INTERVAL;
    }

    @Override
    protected void registerGoals() {
        registerTargetGoals();
        registerBehaviourGoals();
    }

    private void registerTargetGoals() {
        Map<Integer, Goal> targets = new TreeMap<>();
        targets.put(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        targets.put(PLAYER_PRIORITY, stalk(Player.class));
        targets.put(CULTIST_PRIORITY, stalk(EntityCultist.class));
        targets.forEach(this.targetSelector::addGoal);
    }

    private <T extends LivingEntity> Goal stalk(Class<T> quarry) {
        return new NearestAttackableTargetGoal<>(this, quarry, true);
    }

    private void registerBehaviourGoals() {
        Map<Integer, Goal> behaviours = new TreeMap<>();
        behaviours.put(LEAP_PRIORITY, new LeapAtTargetGoal(this, LEAP_STRENGTH));
        behaviours.put(MELEE_PRIORITY, new MeleeAttackGoal(this, MELEE_SPEED, false));
        behaviours.put(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(this, STROLL_SPEED));
        behaviours.put(LOOK_PLAYER_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        behaviours.put(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(this));
        behaviours.forEach(this.goalSelector::addGoal);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HELM, false);
        super.defineSynchedData(builder);
    }

    public boolean hasHelm() {
        Boolean worn = this.entityData.get(HELM);
        return worn.booleanValue();
    }

    @Override
    public void setCanPickUpLoot(boolean canPickUpLoot) {
        super.setCanPickUpLoot(false);
    }

    public int shellBreakTicks() {
        return this.shellBreakTicks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (!HELM.equals(accessor) || !this.level().isClientSide()) {
            return;
        }
        boolean nowHelmed = this.hasHelm();
        if (this.shownHelm && !nowHelmed) {
            this.shellBreakTicks = SHELL_BREAK_TICKS;
        }
        this.shownHelm = nowHelmed;
    }

    public void setHelm(boolean helm) {
        this.entityData.set(HELM, helm);
        double armor = BARE_ARMOR;
        double speed = BARE_SPEED;
        if (helm) {
            armor = HELM_ARMOR;
            speed = HELM_SPEED;
        }
        AttributeInstance pace = this.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        AttributeInstance plating = this.getAttributes().getInstance(Attributes.ARMOR);
        pace.setBaseValue(speed);
        plating.setBaseValue(armor);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        RandomSource dice = level.getRandom();
        boolean hard = level.getDifficulty() == Difficulty.HARD;
        this.setHelm(hard || dice.nextFloat() < HELM_CHANCE);
        if (hard && dice.nextFloat() < BOON_CHANCE * difficulty.getSpecialMultiplier()) {
            grantBoon(SPAWN_BOONS.get(dice.nextInt(SPAWN_BOONS.size())));
        }
        return data;
    }

    private void grantBoon(Holder<MobEffect> boon) {
        this.addEffect(new MobEffectInstance(boon, MobEffectInstance.INFINITE_DURATION, 0));
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force, boolean sendEventAndTriggers) {
        boolean mounted = super.startRiding(vehicle, force, sendEventAndTriggers);
        if (mounted) {
            refreshPassengers(vehicle);
        }
        return mounted;
    }

    private static void refreshPassengers(Entity vehicle) {
        if (vehicle instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetPassengersPacket(player));
        }
    }

    @Override
    public void removeVehicle() {
        Entity previous = this.getVehicle();
        super.removeVehicle();
        refreshPassengers(previous);
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        if (vehicle instanceof LivingEntity) {
            return vehicle.getPassengerRidingPosition(this).subtract(headSeat(vehicle));
        }
        return super.getVehicleAttachmentPoint(vehicle);
    }

    private Vec3 headSeat(Entity host) {
        double reach = SEAT_WIDTH_FACTOR * host.getBbWidth() + SEAT_CRAB_WIDTH_FACTOR * this.getBbWidth();
        Vec3 ahead = host.getLookAngle().scale(reach);
        return host.getEyePosition().add(ahead.x, ahead.y - this.getBbHeight() / 2.0, ahead.z);
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof LivingEntity host) {
            faceYaw(host.getYHeadRot() + FACE_VICTIM_TURN);
        }
    }

    private void faceYaw(float yaw) {
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        this.setYRot(yaw);
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount < FALL_GRACE_TICKS) {
            this.resetFallDistance();
        }
        if (this.level() instanceof ServerLevel level) {
            latchOntoTarget();
            strikeVehicle(level);
        } else {
            this.shellBreakTicks = Math.max(0, this.shellBreakTicks - 1);
        }
        this.attackTimer = Math.max(0, this.attackTimer - 1);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (!super.doHurtTarget(level, target)) {
            return false;
        }
        this.playSound(TTSounds.CRABCLAW.get(), CLAW_VOLUME, CLAW_PITCH_BASE + this.getRandom().nextFloat() * CLAW_PITCH_SPREAD);
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && shellIsCracked()) {
            this.setHelm(false);
            scatterHelmShards(level);
        }
        return hurt;
    }

    private boolean shellIsCracked() {
        float remaining = this.getHealth() / this.getMaxHealth();
        return remaining <= HELM_BREAK_HEALTH && this.hasHelm();
    }

    private void scatterHelmShards(ServerLevel level) {
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, TTItems.CRIMSON_PLATE_CHEST.get());
        double centreY = this.getY() + this.getBbHeight() / 2.0;
        level.sendParticles(shard, this.getX(), centreY, this.getZ(), HELM_BREAK_PARTICLES, HELM_BREAK_SPREAD, HELM_BREAK_SPREAD, HELM_BREAK_SPREAD, HELM_BREAK_SPEED);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        boolean saved = input.getBooleanOr(HELM_KEY, false);
        super.readAdditionalSaveData(input);
        this.setHelm(saved);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean(HELM_KEY, this.hasHelm());
        super.addAdditionalSaveData(output);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity instanceof EntityEldritchCrab || super.considersEntityAsAlly(entity);
    }

    private void latchOntoTarget() {
        LivingEntity prey = this.getTarget();
        if (this.getVehicle() != null || prey == null || this.onGround() || this.hasHelm()) {
            return;
        }
        if (prey.isVehicle() || !prey.isAlive()) {
            return;
        }
        boolean above = this.getY() - prey.getY() >= prey.getBbHeight() / 2.0;
        if (above && this.distanceToSqr(prey) < LATCH_SQUARED_DISTANCE) {
            this.startRiding(prey);
        }
    }

    private void strikeVehicle(ServerLevel level) {
        Entity host = this.getVehicle();
        if (host == null || !this.isAlive() || this.attackTimer > 0) {
            return;
        }
        this.doHurtTarget(level, host);
        this.attackTimer = MOUNTED_ATTACK_BASE + this.getRandom().nextInt(MOUNTED_ATTACK_SPREAD);
        if (this.getRandom().nextFloat() < DISMOUNT_CHANCE) {
            this.stopRiding();
        }
    }
}
