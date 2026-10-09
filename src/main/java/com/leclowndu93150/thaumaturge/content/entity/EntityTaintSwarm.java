package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.particle.TaintSwarmParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EntityTaintSwarm extends Monster {
    private static final EntityDataAccessor<Boolean> SUMMONED = SynchedEntityData.defineId(EntityTaintSwarm.class, EntityDataSerializers.BOOLEAN);
    private static final String SUMMONED_KEY = "is_summoned";
    private static final String BONUS_DAMAGE_KEY = "bonus_damage";
    private static final String LEGACY_SUMMONED_KEY = "Summoned";
    private static final String LEGACY_BONUS_DAMAGE_KEY = "DamBonus";
    private static final double MAX_HEALTH = 30.0;
    private static final double ATTACK_DAMAGE = 2.0;
    private static final double FLYING_SPEED = 0.6;
    private static final double MOVEMENT_SPEED = 0.3;
    private static final double FOLLOW_RANGE = 8.0;
    private static final int EXPERIENCE_REWARD = 4;
    private static final int MAX_TURN = 20;
    private static final double VERTICAL_DAMPING = 0.6;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int HUNT_PRIORITY = 2;
    private static final int COOLDOWN_BASE = 15;
    private static final int COOLDOWN_SPREAD = 10;
    private static final float SUMMONED_DECAY_DAMAGE = 5.0F;
    private static final int MAX_FLIGHT_HEIGHT = 8;
    private static final double TARGET_REACHED_DISTANCE_SQR = 4.0;
    private static final int RETARGET_ODDS = 30;
    private static final int TARGET_ATTEMPTS = 8;
    private static final int TARGET_HORIZONTAL_SPREAD = 7;
    private static final int TARGET_VERTICAL_RANGE = 6;
    private static final int TARGET_VERTICAL_BELOW = 2;
    private static final double WANDER_SPEED = 0.55;
    private static final double WANDER_LIFT = 0.1;
    private static final double HUNT_SPEED = 1.0;
    private static final double ATTACK_RANGE_SQR = 9.0;
    private static final int WEAKNESS_DURATION = 100;
    private static final int WEAKNESS_AMPLIFIER = 0;
    private static final float ATTACK_SOUND_VOLUME = 0.3F;
    private static final float ATTACK_SOUND_PITCH_BASE = 0.9F;
    private static final float ATTACK_SOUND_PITCH_SPREAD = 0.2F;
    private static final float SOUND_VOLUME = 0.1F;
    private static final double HALF = 0.5;

    private int attackCooldown;
    private int damageBonus;
    private @Nullable BlockPos flightTarget;

    public EntityTaintSwarm(EntityType<? extends EntityTaintSwarm> type, Level level) {
        super(type, level);
        this.moveControl = new SwarmMoveControl(this);
        this.xpReward = EXPERIENCE_REWARD;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.FLYING_SPEED, FLYING_SPEED)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    public boolean isSummoned() {
        return this.entityData.get(SUMMONED);
    }

    public void setSummoned(boolean summoned) {
        this.entityData.set(SUMMONED, summoned);
    }

    public void setDamBonus(int bonus) {
        this.damageBonus = bonus;
    }

    public float effectiveAttackDamage() {
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return base + this.damageBonus;
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    private static SoundEvent attackSound() {
        return TTSounds.SWARMATTACK.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.SWARM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return attackSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return attackSound();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double heightDiff, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SUMMONED, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SwarmNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(HUNT_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, VERTICAL_DAMPING, 1.0));
        Level world = this.level();
        if (!(world instanceof ServerLevel server)) {
            spawnSwarmCloud(world);
            return;
        }
        serverBehaviour(server);
    }

    private void spawnSwarmCloud(Level world) {
        Vec3 centre = this.position().add(0.0, this.getBbHeight() * HALF, 0.0);
        world.addParticle(new TaintSwarmParticleOptions(this.getId()), centre.x, centre.y, centre.z, 0.0, 0.0, 0.0);
    }

    private void serverBehaviour(ServerLevel server) {
        this.attackCooldown -= Integer.signum(this.attackCooldown);
        LivingEntity prey = livePrey();
        if (prey == null) {
            idle(server);
        } else {
            engage(server, prey);
        }
    }

    private @Nullable LivingEntity livePrey() {
        LivingEntity prey = this.getTarget();
        return prey != null && prey.isAlive() ? prey : null;
    }

    private void engage(ServerLevel server, LivingEntity prey) {
        this.getMoveControl().setWantedPosition(prey.getX(), prey.getEyeY(), prey.getZ(), HUNT_SPEED);
        boolean ready = this.attackCooldown <= 0;
        if (ready && canReach(prey)) {
            bite(server, prey);
        }
    }

    private boolean canReach(LivingEntity prey) {
        return this.distanceToSqr(prey) < ATTACK_RANGE_SQR && this.hasLineOfSight(prey) && overlapsVertically(prey);
    }

    private void idle(ServerLevel server) {
        if (this.isSummoned()) {
            this.hurtServer(server, server.damageSources().generic(), SUMMONED_DECAY_DAMAGE);
        } else {
            wander(server);
        }
    }

    private boolean overlapsVertically(LivingEntity target) {
        AABB mine = this.getBoundingBox();
        AABB theirs = target.getBoundingBox();
        return theirs.minY <= mine.maxY && mine.minY <= theirs.maxY;
    }

    private void bite(ServerLevel level, LivingEntity target) {
        int pause = this.random.nextInt(COOLDOWN_SPREAD);
        this.attackCooldown = COOLDOWN_BASE + pause;
        Vec3 preservedMotion = target.getDeltaMovement();
        boolean hit = this.doHurtTarget(level, target);
        target.setDeltaMovement(preservedMotion);
        if (hit) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, WEAKNESS_AMPLIFIER, true, false, false));
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), TTSounds.SWARMATTACK.get(), SoundSource.HOSTILE, ATTACK_SOUND_VOLUME,
                ATTACK_SOUND_PITCH_BASE + this.random.nextFloat() * ATTACK_SOUND_PITCH_SPREAD);
    }

    private void wander(ServerLevel level) {
        BlockPos goal = currentGoal(level);
        if (goal != null) {
            Vec3 aim = Vec3.atBottomCenterOf(goal).add(0.0, WANDER_LIFT, 0.0);
            this.getMoveControl().setWantedPosition(aim.x, aim.y, aim.z, WANDER_SPEED);
        }
    }

    private @Nullable BlockPos currentGoal(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        if (needsNewFlightTarget(level, origin)) {
            this.flightTarget = pickFlightTarget(level, origin);
        }
        return this.flightTarget;
    }

    private boolean needsNewFlightTarget(ServerLevel level, BlockPos origin) {
        BlockPos current = this.flightTarget;
        if (current == null || !isValidFlightTarget(level, current)) {
            return true;
        }
        return current.distSqr(origin) < TARGET_REACHED_DISTANCE_SQR || this.random.nextInt(RETARGET_ODDS) == 0;
    }

    private @Nullable BlockPos pickFlightTarget(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < TARGET_ATTEMPTS; attempt++) {
            BlockPos candidate = origin.offset(this.random.nextInt(TARGET_HORIZONTAL_SPREAD) - this.random.nextInt(TARGET_HORIZONTAL_SPREAD),
                    this.random.nextInt(TARGET_VERTICAL_RANGE) - TARGET_VERTICAL_BELOW, this.random.nextInt(TARGET_HORIZONTAL_SPREAD) - this.random.nextInt(TARGET_HORIZONTAL_SPREAD));
            if (isValidFlightTarget(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isValidFlightTarget(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.isAir() && pos.getY() > level.getMinY() && pos.getY() <= level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) + MAX_FLIGHT_HEIGHT
                && TaintBiomeManager.isTainted(level, pos);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damageBonus = input.getIntOr(BONUS_DAMAGE_KEY, input.getIntOr(LEGACY_BONUS_DAMAGE_KEY, 0));
        boolean storedSummoned = input.getBooleanOr(SUMMONED_KEY, input.getBooleanOr(LEGACY_SUMMONED_KEY, false));
        this.setSummoned(storedSummoned);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(BONUS_DAMAGE_KEY, this.damageBonus);
        output.putBoolean(SUMMONED_KEY, this.isSummoned());
    }

    private static final class SwarmNavigation extends FlyingPathNavigation {
        private SwarmNavigation(EntityTaintSwarm swarm, Level level) {
            super(swarm, level);
            this.setCanOpenDoors(false);
            this.setCanFloat(true);
        }
    }

    private static final class SwarmMoveControl extends FlyingMoveControl {
        private SwarmMoveControl(EntityTaintSwarm swarm) {
            super(swarm, MAX_TURN, false);
        }

        @Override
        public void tick() {
            super.tick();
            this.mob.setNoGravity(true);
        }
    }
}
