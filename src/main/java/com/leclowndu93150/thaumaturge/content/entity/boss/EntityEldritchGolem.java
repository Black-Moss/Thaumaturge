package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.world.mound.BlockLoot;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchGolem extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_HEADLESS = SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);
    private static final String HEADLESS_KEY = "headless";
    private static final String NAME_KEY = "entity.thaumaturge.eldritch_golem.name.custom";
    private static final double MAX_HEALTH = 400.0;
    private static final double ATTACK_DAMAGE = 10.0;
    private static final double MOVEMENT_SPEED = 0.3;
    private static final double ARMOR = 6.0;
    private static final float HEADLESS_EYE_HEIGHT = 3.2F;
    private static final int EMERGENCE_TICKS = 100;
    private static final float EMERGENCE_HEALING = 2.0F;
    private static final int FLOAT_PRIORITY = 0;
    private static final int RANGED_PRIORITY = 2;
    private static final int MELEE_PRIORITY = 3;
    private static final int HOME_PRIORITY = 5;
    private static final int STROLL_PRIORITY = 6;
    private static final int LOOK_PLAYER_PRIORITY = 7;
    private static final int LOOK_AROUND_PRIORITY = 8;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final double MELEE_SPEED = 1.1;
    private static final double HOME_SPEED = 0.8;
    private static final double STROLL_SPEED = 0.6;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final double BEAM_STANCE_SPEED = 1.0;
    private static final double BEAM_MIN_DISTANCE = 3.0;
    private static final int BEAM_INTERVAL = 5;
    private static final float BEAM_RADIUS = 24.0F;
    private static final byte SWING_EVENT = 4;
    private static final int SWING_COOLDOWN = 10;
    private static final float SWING_DAMAGE_FACTOR = 0.75F;
    private static final double SWING_TOSS = 0.25;
    private static final double HEADLESS_SHOVE = 1.6;
    private static final double HEADLESS_EXTRA_LIFT = 0.1;
    private static final float SWING_VOLUME = 1.0F;
    private static final float SWING_PITCH = 1.0F;
    private static final float HEAD_BLAST_POWER = 2.0F;
    private static final double HEAD_BLAST_REACH = 0.6;
    private static final boolean HEAD_BLAST_FIRE = false;
    private static final double STRIDE_EPSILON_SQR = 1.0E-4;
    private static final int TRAMPLE_ODDS = 5;
    private static final int DUST_COUNT = 6;
    private static final double DUST_SPREAD_FACTOR = 0.4;
    private static final double DUST_RISE = 0.1;
    private static final double DUST_HEIGHT_SPREAD = 0.05;
    private static final double DUST_SPEED = 0.15;
    private static final double FLOOR_PROBE = 0.2;
    private static final float FRAGILE_HARDNESS = 0.15F;
    private static final double NECK_HEIGHT = 0.95;
    private static final int SMOKE_INTERVAL = 2;
    private static final double SMOKE_SPREAD = 0.1;
    private static final double SMOKE_RISE = 0.06;
    private static final int SPARK_ODDS = 10;
    private static final int SPARK_COLOR = 0xFFE6A0;
    private static final float SPARK_ALPHA = 0.9F;
    private static final float SPARK_SCALE = 3.0F;

    private final GolemBeamCore beam = new GolemBeamCore(this);
    private boolean beamStanceArmed;
    private int swingCooldown;

    public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.ARMOR, ARMOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEADLESS, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(FLOAT_PRIORITY, new FloatGoal(this));
        goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MELEE_SPEED, true));
        goalSelector.addGoal(HOME_PRIORITY, new MoveTowardsRestrictionGoal(this, HOME_SPEED));
        goalSelector.addGoal(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(this, STROLL_SPEED));
        goalSelector.addGoal(LOOK_PLAYER_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        goalSelector.addGoal(LOOK_AROUND_PRIORITY, new RandomLookAroundGoal(this));
        targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private void armBeamStance() {
        if (beamStanceArmed) {
            return;
        }
        beamStanceArmed = true;
        goalSelector.addGoal(RANGED_PRIORITY, new LongRangeAttackGoal(this, BEAM_STANCE_SPEED, BEAM_MIN_DISTANCE, BEAM_INTERVAL, BEAM_INTERVAL, BEAM_RADIUS));
    }

    public boolean isHeadless() {
        return entityData.get(DATA_HEADLESS);
    }

    private void setHeadless(boolean headless) {
        entityData.set(DATA_HEADLESS, headless);
        if (headless) {
            armBeamStance();
        }
    }

    public int swingCooldown() {
        return swingCooldown;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_HEADLESS.equals(accessor)) {
            refreshDimensions();
        }
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDefaultDimensions(pose);
        return isHeadless() ? dimensions.withEyeHeight(HEADLESS_EYE_HEIGHT) : dimensions;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        ChampionHelper.makeChampion(this, true);
        spawnTimer = EMERGENCE_TICKS;
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public void assignTitle() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(Component.translatable(NAME_KEY, MobTraitNames.of(trait))));
    }

    @Override
    public void tick() {
        super.tick();
        if (swingCooldown > 0) {
            swingCooldown--;
        }
        if (!(level() instanceof ServerLevel level) || !isAlive()) {
            return;
        }
        if (emergenceTicks() > 0) {
            heal(EMERGENCE_HEALING);
        }
        if (isHeadless()) {
            beam.tick(level);
            ventNeck(level);
        }
        trample(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isHeadless()) {
            setXRot(0.0F);
        }
    }

    private void trample(ServerLevel level) {
        double strideX = getX() - xo;
        double strideZ = getZ() - zo;
        BlockPos feet = blockPosition();
        if (strideX * strideX + strideZ * strideZ > STRIDE_EPSILON_SQR && getRandom().nextInt(TRAMPLE_ODDS) == 0) {
            kickDust(level);
            smashLoot(level, feet);
            smashLoot(level, feet.below());
        }
        crushFragile(level, feet);
    }

    private void kickDust(ServerLevel level) {
        BlockPos floor = BlockPos.containing(getX(), getY() - FLOOR_PROBE, getZ());
        BlockState ground = level.getBlockState(floor);
        if (ground.isAir()) {
            return;
        }
        double spread = getBbWidth() * DUST_SPREAD_FACTOR;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + DUST_RISE, getZ(), DUST_COUNT, spread, DUST_HEIGHT_SPREAD, spread, DUST_SPEED);
    }

    private void smashLoot(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof BlockLoot) {
            level.destroyBlock(pos, true, this);
        }
    }

    private void crushFragile(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= FRAGILE_HARDNESS) {
            level.destroyBlock(pos, true, this);
        }
    }

    private void ventNeck(ServerLevel level) {
        if (tickCount % SMOKE_INTERVAL != 0) {
            return;
        }
        double neckY = getY() + getBbHeight() * NECK_HEIGHT;
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), neckY, getZ(), 0, getRandom().triangle(0.0, SMOKE_SPREAD), SMOKE_RISE, getRandom().triangle(0.0, SMOKE_SPREAD), 1.0);
        if (getRandom().nextInt(SPARK_ODDS) == 0) {
            level.sendParticles(new SparkParticleOptions(SPARK_COLOR, SPARK_ALPHA, SPARK_SCALE), getX(), neckY, getZ(), 1, SMOKE_SPREAD, SMOKE_SPREAD, SMOKE_SPREAD, 0.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (opensNeck(level, source, damage)) {
            blowOffHead(level);
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    private boolean opensNeck(ServerLevel level, DamageSource source, float damage) {
        return !isHeadless() && isAlive() && damage > getHealth() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !isInvulnerableTo(level, source);
    }

    private void blowOffHead(ServerLevel level) {
        spawnTimer = EMERGENCE_TICKS;
        setHeadless(true);
        Vec3 ahead = Vec3.directionFromRotation(0.0F, getYHeadRot()).scale(getBbWidth() / 2.0 + HEAD_BLAST_REACH);
        level.explode(this, getX() + ahead.x, getEyeY(), getZ() + ahead.z, HEAD_BLAST_POWER, HEAD_BLAST_FIRE, Level.ExplosionInteraction.NONE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (swingCooldown > 0) {
            return false;
        }
        swingCooldown = SWING_COOLDOWN;
        level.broadcastEntityEvent(this, SWING_EVENT);
        DamageSource source = damageSources().mobAttack(this);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * SWING_DAMAGE_FACTOR;
        boolean hurt = target.hurtServer(level, source, damage);
        if (hurt) {
            fling(target);
            EnchantmentHelper.doPostAttackEffects(level, target, source);
            setLastHurtMob(target);
        }
        playSound(SoundEvents.IRON_GOLEM_ATTACK, SWING_VOLUME, SWING_PITCH);
        return hurt;
    }

    private void fling(Entity target) {
        double resistance = target instanceof LivingEntity living ? living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 0.0;
        double give = Math.max(0.0, 1.0 - resistance);
        if (isHeadless() && target instanceof LivingEntity living) {
            float yaw = getYRot() * Mth.DEG_TO_RAD;
            living.knockback(HEADLESS_SHOVE, Mth.sin(yaw), -Mth.cos(yaw));
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, HEADLESS_EXTRA_LIFT * give, 0.0));
        }
        target.setDeltaMovement(target.getDeltaMovement().add(0.0, SWING_TOSS * give, 0.0));
        target.hurtMarked = true;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (isHeadless() && !holdsStill() && level() instanceof ServerLevel level) {
            beam.fire(level, target);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == SWING_EVENT) {
            swingCooldown = SWING_COOLDOWN;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, SWING_VOLUME, SWING_PITCH);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(HEADLESS_KEY, isHeadless());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setHeadless(input.getBooleanOr(HEADLESS_KEY, false));
    }
}
