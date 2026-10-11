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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;

public final class ThaumicSlime extends Slime {
    private static final EntityDataAccessor<Integer> LAUNCH_TIMER = SynchedEntityData.defineId(ThaumicSlime.class, EntityDataSerializers.INT);
    private static final int[] SPAWN_SIZES = {1, 2, 4};
    private static final int LAUNCH_TICKS = 10;
    private static final int PARTICLE_COLOR = 0x6A1E8C;
    private static final float PARTICLE_ALPHA = 0.6F;
    private static final int DROPLET_LIFETIME = 0;
    private static final int DROPLETS_PER_LAUNCH_TICK = 2;
    private static final double DROPLET_SCATTER = 0.05;
    private static final double DROPLET_RING_SPEED = 0.08;
    private static final double DROPLET_RING_LIFT = 0.1;
    private static final double MID_HEIGHT = 0.5;
    private static final int SPLIT_CHILD_SIZE = 1;
    private static final double SPLIT_SCATTER_DIVISOR = 4.0;
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

    private boolean sizeAssigned;
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
        sizeAssigned = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            spawnTrail();
            return;
        }
        if (!sizeAssigned) {
            setSize(SPAWN_SIZES[random.nextInt(SPAWN_SIZES.length)], true);
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
        int count = entityData.get(LAUNCH_TIMER) * DROPLETS_PER_LAUNCH_TICK;
        AABB box = getBoundingBox();
        for (int i = 0; i < count; i++) {
            double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
            double y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
            double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
            level().addParticle(getParticleType(), x, y, z, scatter(), scatter(), scatter());
        }
    }

    private double scatter() {
        return (random.nextDouble() - random.nextDouble()) * DROPLET_SCATTER;
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
        int count = getSize() * LANDING_DROPLETS_PER_SIZE;
        double radius = getBbWidth() * MID_HEIGHT;
        double y = getY(MID_HEIGHT);
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            double dx = Mth.sin(angle);
            double dz = Mth.cos(angle);
            level().addParticle(getParticleType(), getX() + dx * radius, y, getZ() + dz * radius, dx * DROPLET_RING_SPEED, DROPLET_RING_LIFT, dz * DROPLET_RING_SPEED);
        }
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!landingSoundPending) {
            super.playSound(sound, volume, pitch);
            return;
        }
        landingSoundPending = false;
        super.playSound(sound, volume, ((random.nextFloat() - random.nextFloat()) * PITCH_SPREAD + ATTACK_PITCH_BASE) * HOP_PITCH_FACTOR);
    }

    @Override
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(LAUNCH_TIMER, 0);
    }

    @Override
    protected ParticleOptions getParticleType() {
        return new FluxGooDropletParticleOptions(PARTICLE_COLOR, PARTICLE_ALPHA, DROPLET_LIFETIME);
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
        double scatter = count / SPLIT_SCATTER_DIVISOR;
        double y = getY() + SPLIT_LIFT;
        PlayerTeam team = getTeam();
        setSize(SPLIT_CHILD_SIZE, false);
        for (int i = 0; i < count; i++) {
            double x = getX() + (random.nextDouble() * 2.0 - 1.0) * scatter;
            double z = getZ() + (random.nextDouble() * 2.0 - 1.0) * scatter;
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
