package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.INT);
    private static final int NO_TARGET = -1;
    private static final double EYE_DROP = 0.1;
    private static final int RED_LIFETIME = 240;
    private static final int WHITE_LIFETIME = 160;
    private static final float RED_DAMAGE_FACTOR = 1.0F;
    private static final float WHITE_DAMAGE_FACTOR = 0.6F;
    private static final double HOMING_STRENGTH = 0.2;
    private static final double AIM_HEIGHT_FACTOR = 0.6;
    private static final double MAX_SPEED = 0.25;
    private static final double DEFLECT_SPEED = 0.9;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH_BASE = 1.0F;
    private static final float SOUND_PITCH_SPREAD = 0.2F;

    private @Nullable LivingEntity target;

    public EntityGolemOrb(EntityType<? extends EntityGolemOrb> type, Level level) {
        super(type, level);
    }

    public EntityGolemOrb(Level level, LivingEntity owner, LivingEntity target, boolean red) {
        super(TTEntities.GOLEM_ORB.get(), owner.getX(), owner.getEyeY() - EYE_DROP, owner.getZ(), level);
        this.setOwner(owner);
        this.target = target;
        this.entityData.set(TARGET_ID, target.getId());
        this.entityData.set(RED, red);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RED, false);
        builder.define(TARGET_ID, NO_TARGET);
    }

    public boolean isRed() {
        return this.entityData.get(RED);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (!this.level().isClientSide() && this.tickCount > (this.isRed() ? RED_LIFETIME : WHITE_LIFETIME)) {
            this.discard();
            return;
        }
        home();
    }

    private void home() {
        if (this.target == null) {
            int id = this.entityData.get(TARGET_ID);
            if (id != NO_TARGET && this.level().getEntity(id) instanceof LivingEntity living) {
                this.target = living;
            }
        }
        if (this.target == null || !this.target.isAlive()) {
            return;
        }
        double distanceSqr = this.distanceToSqr(this.target);
        if (distanceSqr == 0.0) {
            return;
        }
        Vec3 pull = new Vec3(this.target.getX() - this.getX(), this.target.getBoundingBox().minY + this.target.getBbHeight() * AIM_HEIGHT_FACTOR - this.getY(), this.target.getZ() - this.getZ())
                .scale(HOMING_STRENGTH / distanceSqr);
        Vec3 velocity = this.getDeltaMovement().add(pull);
        this.setDeltaMovement(Mth.clamp(velocity.x, -MAX_SPEED, MAX_SPEED), Mth.clamp(velocity.y, -MAX_SPEED, MAX_SPEED), Mth.clamp(velocity.z, -MAX_SPEED, MAX_SPEED));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (result instanceof EntityHitResult hit && this.getOwner() instanceof LivingEntity owner) {
            float factor = this.isRed() ? RED_DAMAGE_FACTOR : WHITE_DAMAGE_FACTOR;
            hit.getEntity().hurtServer(server, server.damageSources().indirectMagic(this, owner), (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor);
        }
        playSound(server, true);
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        this.setDeltaMovement(attacker.getLookAngle().scale(DEFLECT_SPEED));
        this.hurtMarked = true;
        playSound(level, false);
        return true;
    }

    private void playSound(ServerLevel level, boolean shock) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), shock ? TTSounds.SHOCK.get() : TTSounds.ZAP.get(), SoundSource.HOSTILE, SOUND_VOLUME,
                SOUND_PITCH_BASE + (this.random.nextFloat() - this.random.nextFloat()) * SOUND_PITCH_SPREAD);
    }
}
