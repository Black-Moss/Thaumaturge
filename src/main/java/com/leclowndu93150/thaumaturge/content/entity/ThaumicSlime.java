package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.ThaumicSlimeSpitGoal;
import com.leclowndu93150.thaumaturge.content.particle.FluxGooDropletParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public final class ThaumicSlime extends Slime {
    private static final EntityDataAccessor<Integer> LAUNCH_TIMER = SynchedEntityData.defineId(ThaumicSlime.class, EntityDataSerializers.INT);
    private static final int SPAWN_SIZE_BASE_SHIFT = 1;
    private static final int SPAWN_SIZE_VARIANTS = 3;
    private static final int LAUNCH_TICKS = 10;
    private static final int PARTICLE_COLOR = 0xB200FF;
    private static final float PARTICLE_ALPHA = 0.4F;
    private static final int DEFAULT_DROPLET_LIFETIME = 0;
    private static final double DROPLET_LIFETIME_NUMERATOR = 66.0;
    private static final double DROPLET_LIFETIME_RANDOM_SCALE = 0.9;
    private static final double DROPLET_LIFETIME_RANDOM_FLOOR = 0.1;
    private static final int LAUNCH_COUNT_BONUS = 1;
    private static final float RING_RADIUS_PER_SIZE = 0.5F;
    private static final float RING_FACTOR_MIN = 0.5F;
    private static final float RING_FACTOR_RANGE = 0.5F;
    private static final double MID_HEIGHT = 0.5;
    private static final int SPLIT_CHILD_SIZE = 1;
    private static final double SPLIT_SPACING_DIVISOR = 4.0;
    private static final int SPLIT_GRID_COLUMNS = 2;
    private static final double SPLIT_GRID_CENTER = 0.5;
    private static final double SPLIT_LIFT = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final int EXPERIENCE_BONUS = 2;
    private static final float ATTACK_DAMAGE_BONUS = 1.0F;
    private static final double REACH_PER_SIZE = 0.6;
    private static final int LAUNCH_REACH_BONUS = 2;
    private static final float ATTACK_VOLUME = 1.0F;
    private static final float ATTACK_PITCH_BASE = 1.0F;
    private static final float PITCH_SPREAD = 0.2F;
    private static final float HOP_PITCH_FACTOR = 0.8F;
    private static final int LANDING_DROPLETS_PER_SIZE = 2;
    private static final double MAX_HEALTH_PLACEHOLDER = 1.0;
    private static final double SPEED_PLACEHOLDER = 0.0;
    private static final double DAMAGE_PLACEHOLDER = 0.0;
    private static final int SPIT_PRIORITY = 3;
    private static final int SPIT_MIN_SIZE_EXCLUSIVE = 2;
    private static final double SPIT_MIN_DISTANCE_SQR = 16.0;

    private boolean landingSoundPending;

    public ThaumicSlime(EntityType<? extends ThaumicSlime> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH_PLACEHOLDER).add(Attributes.MOVEMENT_SPEED, SPEED_PLACEHOLDER).add(Attributes.ATTACK_DAMAGE, DAMAGE_PLACEHOLDER);
    }

    public static boolean checkSpawnRules(EntityType<ThaumicSlime> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAUNCH_TIMER, LAUNCH_TICKS);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(SPIT_PRIORITY, new ThaumicSlimeSpitGoal(this));
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        xpReward = getSize() + EXPERIENCE_BONUS;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        setSize(1 << (SPAWN_SIZE_BASE_SHIFT + level.getRandom().nextInt(SPAWN_SIZE_VARIANTS)), true);
        return groupData;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            spawnTrail();
            return;
        }
        tickLaunch();
    }

    private void tickLaunch() {
        int left = entityData.get(LAUNCH_TIMER);
        if (left > 0) {
            entityData.set(LAUNCH_TIMER, left - 1);
        }
    }

    private void spawnTrail() {
        int timer = entityData.get(LAUNCH_TIMER);
        if (timer > 0) {
            spawnRingDroplets(getSize() * (timer + LAUNCH_COUNT_BONUS));
        }
    }

    private ParticleOptions dropletOptions() {
        int lifetime = (int) (DROPLET_LIFETIME_NUMERATOR / (random.nextDouble() * DROPLET_LIFETIME_RANDOM_SCALE + DROPLET_LIFETIME_RANDOM_FLOOR));
        return new FluxGooDropletParticleOptions(PARTICLE_COLOR, PARTICLE_ALPHA, lifetime);
    }

    @Override
    protected boolean spawnCustomParticles() {
        landingSoundPending = true;
        if (level().isClientSide()) {
            spawnDroplets();
        }
        return true;
    }

    private void spawnDroplets() {
        spawnRingDroplets(getSize() * LANDING_DROPLETS_PER_SIZE);
    }

    private void spawnRingDroplets(int count) {
        double y = getY(MID_HEIGHT);
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float factor = RING_FACTOR_MIN + random.nextFloat() * RING_FACTOR_RANGE;
            double radius = getSize() * RING_RADIUS_PER_SIZE * factor;
            level().addParticle(dropletOptions(), getX() + Mth.sin(angle) * radius, y, getZ() + Mth.cos(angle) * radius, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!landingSoundPending) {
            super.playSound(sound, volume, pitch);
            return;
        }
        landingSoundPending = false;
        super.playSound(getJumpSound(), getSoundVolume(), ((random.nextFloat() - random.nextFloat()) * PITCH_SPREAD + ATTACK_PITCH_BASE) * HOP_PITCH_FACTOR);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(LAUNCH_TIMER, 0);
    }

    @Override
    protected ParticleOptions getParticleType() {
        return new FluxGooDropletParticleOptions(PARTICLE_COLOR, PARTICLE_ALPHA, DEFAULT_DROPLET_LIFETIME);
    }

    @Override
    protected float getAttackDamage() {
        return getSize() + ATTACK_DAMAGE_BONUS;
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        if (level() instanceof ServerLevel server && isAlive() && distanceToSqr(target) < reachSqr() && hasLineOfSight(target)) {
            DamageSource source = damageSources().mobAttack(this);
            if (target.hurtServer(server, source, getAttackDamage())) {
                playSound(SoundEvents.SLIME_ATTACK, ATTACK_VOLUME, (random.nextFloat() - random.nextFloat()) * PITCH_SPREAD + ATTACK_PITCH_BASE);
                EnchantmentHelper.doPostAttackEffects(server, target, source);
            }
        }
    }

    private double reachSqr() {
        double reach = REACH_PER_SIZE * (getSize() + (isLaunched() ? LAUNCH_REACH_BONUS : 0));
        return reach * reach;
    }

    public void markLaunched() {
        entityData.set(LAUNCH_TIMER, LAUNCH_TICKS);
    }

    public boolean isLaunched() {
        return entityData.get(LAUNCH_TIMER) > 0;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide() && getSize() > SPLIT_CHILD_SIZE && isDeadOrDying()) {
            split();
        }
        super.remove(reason);
    }

    private void split() {
        int count = getSize();
        double spacing = count / SPLIT_SPACING_DIVISOR;
        double y = getY() + SPLIT_LIFT;
        PlayerTeam team = getTeam();
        setSize(SPLIT_CHILD_SIZE, false);
        for (int i = 0; i < count; i++) {
            double x = getX() + (i % SPLIT_GRID_COLUMNS - SPLIT_GRID_CENTER) * spacing;
            double z = getZ() + (i / SPLIT_GRID_COLUMNS - SPLIT_GRID_CENTER) * spacing;
            float yaw = random.nextFloat() * FULL_TURN_DEGREES;
            convertTo(TTEntities.THAUMIC_SLIME.get(), new ConversionParams(ConversionType.SPLIT_ON_DEATH, false, false, team), EntitySpawnReason.TRIGGERED, child -> {
                child.setSize(SPLIT_CHILD_SIZE, true);
                child.snapTo(x, y, z, yaw, 0.0F);
            });
        }
    }

    public boolean canSpitAt(Player player) {
        return getSize() > SPIT_MIN_SIZE_EXCLUSIVE && distanceToSqr(player) > SPIT_MIN_DISTANCE_SQR;
    }
}
