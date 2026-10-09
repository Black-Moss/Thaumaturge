package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public class EntityEldritchGolem extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);
    private static final String HEADLESS_KEY = "headless";
    private static final String NAME_KEY = "entity.thaumaturge.eldritch_golem.name.custom";
    private static final byte SWING_EVENT = 4;
    private static final double MOVEMENT_SPEED = 0.3;
    private static final double ATTACK_DAMAGE = 10.0;
    private static final double MAX_HEALTH = 400.0;
    private static final double ARMOR = 6.0;
    private static final int FLOAT_PRIORITY = 0;
    private static final int BEAM_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int HOME_PRIORITY = 6;
    private static final int STROLL_PRIORITY = 7;
    private static final int LOOK_PRIORITY = 8;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_TARGET_PRIORITY = 2;
    private static final double MELEE_SPEED = 1.1;
    private static final double HOME_SPEED = 0.8;
    private static final double STROLL_SPEED = 0.8;
    private static final float LOOK_RANGE = 8.0F;
    private static final double BEAM_SPEED = 1.0;
    private static final double BEAM_MIN_DISTANCE = 3.0;
    private static final int BEAM_INTERVAL = 5;
    private static final float BEAM_RADIUS = 24.0F;
    private static final int AWAKEN_TICKS = 100;
    private static final float AWAKEN_HEALING = 2.0F;
    private static final float HEAD_BLAST_POWER = 2.0F;
    private static final double HEAD_BLAST_OFFSET = 0.75;
    private static final int SWING_TICKS = 10;
    private static final float SWING_DAMAGE_FACTOR = 0.75F;
    private static final double LAUNCH_LIFT = 0.2;
    private static final double HEADLESS_PUSH = 1.5;
    private static final double HEADLESS_LIFT = 0.1;
    private static final float SWING_VOLUME = 1.0F;
    private static final float SWING_PITCH = 1.0F;
    private static final SoundEvent HURT_SOUND = SoundEvents.IRON_GOLEM_HURT;
    private static final SoundEvent DEATH_SOUND = SoundEvents.IRON_GOLEM_DEATH;
    private static final SoundEvent STEP_SOUND = SoundEvents.IRON_GOLEM_STEP;
    private static final float STEP_VOLUME = 1.0F;
    private static final float STEP_PITCH = 1.0F;
    private static final double MOVING_SPEED_SQR = 0.0005 * 0.0005;
    private static final int STOMP_ONE_IN = 5;
    private static final double UNDERFOOT_DEPTH = 0.2;
    private static final double DUST_RING_FRACTION = 0.5;
    private static final double DUST_RING_JITTER = 0.25;
    private static final double DUST_BASE_HEIGHT = 0.05;
    private static final double DUST_LIFT_SIGMA = 0.08;
    private static final double DUST_OUTWARD_SPEED = 0.12;
    private static final double DUST_OUTWARD_JITTER = 0.5;
    private static final double FULL_TURN = Math.PI * 2.0;
    private static final float SOFT_BLOCK_HARDNESS = 0.15F;
    private static final float TORSO_FRACTION = 0.6F;
    private static final double AIM_LEAD_TICKS = 10.0;
    private static final double ORB_CLEARANCE = 1.0;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_SPREAD = 5.0F;
    private static final float ORB_VOLUME = 1.0F;
    private static final float ORB_PITCH_BASE = 1.0F;
    private static final float ORB_PITCH_SPREAD = 0.1F;

    private final GolemBeamCore beam = new GolemBeamCore(this);
    private boolean beamArmed;
    private int swingTicks;

    public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ARMOR, ARMOR);
    }

    @Override
    protected void registerGoals() {
        addBehaviourGoals();
        addTargetingGoals();
    }

    private void addBehaviourGoals() {
        Goal[] ordered = {new FloatGoal(this), new MeleeAttackGoal(this, MELEE_SPEED, false), new MoveTowardsRestrictionGoal(this, HOME_SPEED), new WaterAvoidingRandomStrollGoal(this, STROLL_SPEED),
                new LookAtPlayerGoal(this, Player.class, LOOK_RANGE), new RandomLookAroundGoal(this)};
        int[] priorities = {FLOAT_PRIORITY, MELEE_PRIORITY, HOME_PRIORITY, STROLL_PRIORITY, LOOK_PRIORITY, LOOK_PRIORITY};
        for (int i = 0; i < ordered.length; i++) {
            goalSelector.addGoal(priorities[i], ordered[i]);
        }
    }

    private void addTargetingGoals() {
        targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(PLAYER_TARGET_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder.define(HEADLESS, false));
    }

    public boolean isHeadless() {
        return getEntityData().get(HEADLESS).booleanValue();
    }

    private void setHeadless(boolean headless) {
        entityData.set(HEADLESS, headless);
    }

    public int swingCooldown() {
        return swingTicks;
    }

    private void equipBeamGoal() {
        if (!beamArmed) {
            beamArmed = true;
            goalSelector.addGoal(BEAM_PRIORITY, new LongRangeAttackGoal(this, BEAM_SPEED, BEAM_MIN_DISTANCE, BEAM_INTERVAL, BEAM_INTERVAL, BEAM_RADIUS));
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        ChampionHelper.makeChampion(this, true);
        spawnTimer = AWAKEN_TICKS;
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        return result;
    }

    @Override
    public void assignTitle() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(Component.translatable(NAME_KEY, MobTraitNames.of(trait))));
    }

    @Override
    public void tick() {
        if (level() instanceof ServerLevel server) {
            if (spawnTimer > 0) {
                heal(AWAKEN_HEALING);
            }
            super.tick();
            if (isHeadless()) {
                beam.tick(server);
            }
        } else {
            super.tick();
        }
        if (swingTicks > 0) {
            swingTicks--;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        boolean stomping = rollStomp();
        if (level() instanceof ServerLevel server) {
            trample(server, stomping);
        } else if (stomping) {
            scatterDust();
        }
    }

    private boolean rollStomp() {
        return getDeltaMovement().horizontalDistanceSqr() > MOVING_SPEED_SQR && random.nextInt(STOMP_ONE_IN) == 0;
    }

    private void trample(ServerLevel level, boolean stomping) {
        if (!EventHooks.canEntityGrief(level, this)) {
            return;
        }
        if (stomping) {
            breakWhen(level, underfoot(), EntityEldritchGolem::isLootCrate);
        }
        BlockPos cell = blockPosition();
        breakWhen(level, cell, state -> isBrittle(state, level, cell));
    }

    private void breakWhen(ServerLevel level, BlockPos pos, Predicate<BlockState> condition) {
        if (condition.test(level.getBlockState(pos))) {
            level.destroyBlock(pos, true, this);
        }
    }

    private static boolean isLootCrate(BlockState state) {
        return state.is(TTBlocks.LOOT_CRATE_COMMON) || state.is(TTBlocks.LOOT_CRATE_UNCOMMON) || state.is(TTBlocks.LOOT_CRATE_RARE);
    }

    private static boolean isBrittle(BlockState state, ServerLevel level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        return !state.isAir() && hardness >= 0.0F && hardness <= SOFT_BLOCK_HARDNESS;
    }

    private BlockPos underfoot() {
        return BlockPos.containing(getX(), Mth.floor(getY() - UNDERFOOT_DEPTH), getZ());
    }

    private void scatterDust() {
        BlockState state = level().getBlockState(underfoot());
        if (state.isAir()) {
            return;
        }
        double angle = random.nextDouble() * FULL_TURN;
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        double radius = getBbWidth() * (DUST_RING_FRACTION + random.nextDouble() * DUST_RING_JITTER);
        double outward = DUST_OUTWARD_SPEED * (1.0 - DUST_OUTWARD_JITTER + random.nextDouble() * DUST_OUTWARD_JITTER);
        double lift = Math.abs(random.nextGaussian()) * DUST_LIFT_SIGMA;
        level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), getX() + dirX * radius, getY() + DUST_BASE_HEIGHT, getZ() + dirZ * radius, dirX * outward, lift, dirZ * outward);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!isHeadless() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !isInvulnerableTo(level, source) && getHealth() - damage <= 0.0F) {
            shedHead(level);
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    private void shedHead(ServerLevel level) {
        setHeadless(true);
        spawnTimer = AWAKEN_TICKS;
        equipBeamGoal();
        Vec3 facing = Vec3.directionFromRotation(0.0F, getYRot());
        level.explode(this, getX() + facing.x * HEAD_BLAST_OFFSET, getEyeY(), getZ() + facing.z * HEAD_BLAST_OFFSET, HEAD_BLAST_POWER, false, Level.ExplosionInteraction.NONE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        swingTicks = SWING_TICKS;
        level.broadcastEntityEvent(this, SWING_EVENT);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, SWING_VOLUME, SWING_PITCH);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * SWING_DAMAGE_FACTOR;
        boolean hit = target.hurtServer(level, damageSources().mobAttack(this), damage);
        if (hit) {
            Vec3 push = new Vec3(0.0, LAUNCH_LIFT, 0.0);
            if (isHeadless()) {
                Vec3 facing = Vec3.directionFromRotation(0.0F, getYRot());
                push = push.add(facing.x * HEADLESS_PUSH, HEADLESS_LIFT, facing.z * HEADLESS_PUSH);
            }
            target.push(push.x, push.y, push.z);
            target.hurtMarked = true;
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(level() instanceof ServerLevel level) || spawnTimer > 0 || !beam.ready() || !hasLineOfSight(target)) {
            return;
        }
        beam.spend(random);
        EntityGolemOrb orb = new EntityGolemOrb(level, this, target, false);
        Vec3 drift = target.getDeltaMovement();
        Vec3 aim = new Vec3(target.getX() + drift.x * AIM_LEAD_TICKS - orb.getX(), target.getY(TORSO_FRACTION) + drift.y * AIM_LEAD_TICKS - orb.getY(),
                target.getZ() + drift.z * AIM_LEAD_TICKS - orb.getZ());
        Vec3 clearance = aim.normalize().scale(ORB_CLEARANCE);
        orb.setPos(orb.getX() + clearance.x, orb.getY() + clearance.y, orb.getZ() + clearance.z);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_SPREAD);
        level.addFreshEntity(orb);
        level.playSound(null, getX(), getY(), getZ(), TTSounds.EGATTACK.get(), SoundSource.HOSTILE, ORB_VOLUME, ORB_PITCH_BASE + random.nextFloat() * ORB_PITCH_SPREAD);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == SWING_EVENT) {
            swingTicks = SWING_TICKS;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource cause) {
        return HURT_SOUND;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(STEP_SOUND, STEP_VOLUME, STEP_PITCH);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DEATH_SOUND;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setHeadless(in.getBooleanOr(HEADLESS_KEY, false));
        if (isHeadless()) {
            equipBeamGoal();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putBoolean(HEADLESS_KEY, isHeadless());
    }
}
