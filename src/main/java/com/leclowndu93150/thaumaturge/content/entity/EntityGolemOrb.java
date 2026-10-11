package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityGolemOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> RED = SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.INT);
    private static final int NO_TARGET = -1;
    private static final int RED_LIFETIME = 240;
    private static final int WHITE_LIFETIME = 160;
    private static final float RED_DAMAGE_FACTOR = 1.0F;
    private static final float WHITE_DAMAGE_FACTOR = 0.6F;
    private static final double LAUNCH_DROP = 0.1;
    private static final double AIM_HEIGHT_FRACTION = 0.6;
    private static final double STEER_GAIN = 0.3;
    private static final double STEER_MIN_DISTANCE = 1.0;
    private static final double MAX_AXIS_SPEED = 0.25;
    private static final double DEFLECT_SPEED = 0.9;
    private static final float PICK_RADIUS = 0.5F;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH_BASE = 1.0F;
    private static final float SOUND_PITCH_SPREAD = 0.2F;
    private static final int RED_SPARK_COLOR = 0xFF5040;
    private static final int WHITE_SPARK_COLOR = 0xD8E4FF;
    private static final float SPARK_ALPHA = 0.9F;
    private static final float SPARK_SCALE = 2.0F;
    private static final int SPARK_COUNT = 8;
    private static final double SPARK_SPREAD = 0.15;
    private static final double SPARK_SPEED = 0.02;

    public EntityGolemOrb(EntityType<? extends EntityGolemOrb> type, Level level) {
        super(type, level);
    }

    public EntityGolemOrb(Level level, LivingEntity owner, LivingEntity target, boolean red) {
        super(TTEntities.GOLEM_ORB.get(), owner.getX(), owner.getEyeY() - LAUNCH_DROP, owner.getZ(), level);
        setOwner(owner);
        entityData.set(RED, red);
        entityData.set(TARGET, target.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RED, false);
        builder.define(TARGET, NO_TARGET);
    }

    public boolean isRed() {
        return entityData.get(RED);
    }

    private int lifetime() {
        return isRed() ? RED_LIFETIME : WHITE_LIFETIME;
    }

    private float damageFactor() {
        return isRed() ? RED_DAMAGE_FACTOR : WHITE_DAMAGE_FACTOR;
    }

    private @Nullable LivingEntity target() {
        int id = entityData.get(TARGET);
        if (id == NO_TARGET) {
            return null;
        }
        return level().getEntity(id) instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && tickCount > lifetime()) {
            discard();
            return;
        }
        LivingEntity target = target();
        if (target != null) {
            home(target);
        }
        super.tick();
    }

    private void home(LivingEntity target) {
        Vec3 aim = new Vec3(target.getX(), target.getY() + target.getBbHeight() * AIM_HEIGHT_FRACTION, target.getZ());
        Vec3 offset = aim.subtract(position());
        double distance = offset.length();
        if (distance <= 0.0) {
            return;
        }
        Vec3 steer = offset.scale(STEER_GAIN / (distance * Math.max(distance, STEER_MIN_DISTANCE)));
        Vec3 motion = getDeltaMovement().add(steer);
        setDeltaMovement(capAxis(motion.x), capAxis(motion.y), capAxis(motion.z));
    }

    private static double capAxis(double speed) {
        return Mth.clamp(speed, -MAX_AXIS_SPEED, MAX_AXIS_SPEED);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof EntityGolemOrb) && super.canHitEntity(entity);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (level() instanceof ServerLevel server && !isRemoved()) {
            impact(server, hitResult.getLocation());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (level() instanceof ServerLevel server && getOwner() instanceof LivingEntity caster && caster.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageFactor();
            hitResult.getEntity().hurtServer(server, damageSources().indirectMagic(this, caster), damage);
        }
    }

    private void impact(ServerLevel level, Vec3 at) {
        playVaried(level, TTSounds.SHOCK.get());
        SparkParticleOptions spark = new SparkParticleOptions(isRed() ? RED_SPARK_COLOR : WHITE_SPARK_COLOR, SPARK_ALPHA, SPARK_SCALE);
        level.sendParticles(spark, at.x, at.y, at.z, SPARK_COUNT, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPEED);
        discard();
    }

    private void playVaried(ServerLevel level, SoundEvent sound) {
        float pitch = SOUND_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * SOUND_PITCH_SPREAD;
        level.playSound(null, getX(), getY(), getZ(), sound, SoundSource.HOSTILE, SOUND_VOLUME, pitch);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return PICK_RADIUS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (isInvulnerableToBase(source) || attacker == null) {
            return false;
        }
        setDeltaMovement(attacker.getLookAngle().scale(DEFLECT_SPEED));
        markHurt();
        playVaried(level, TTSounds.ZAP.get());
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
