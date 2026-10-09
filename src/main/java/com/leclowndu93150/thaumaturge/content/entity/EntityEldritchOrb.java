package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityEldritchOrb extends ThrowableProjectile {
    private static final int LIFETIME_TICKS = 100;
    private static final double EYE_DROP = 0.1;
    private static final float DAMAGE_FACTOR = 0.666F;
    private static final double BLAST_REACH = 2.0;
    private static final int WEAKNESS_TICKS = 160;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH_BASE = 2.6F;
    private static final float SOUND_PITCH_SPREAD = 0.8F;

    public EntityEldritchOrb(EntityType<? extends EntityEldritchOrb> type, Level level) {
        super(type, level);
    }

    public EntityEldritchOrb(Level level, LivingEntity shooter) {
        super(TTEntities.ELDRITCH_ORB.get(), shooter.getX(), shooter.getEyeY() - EYE_DROP, shooter.getZ(), level);
        this.setOwner(shooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level) || !(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
            return;
        }
        float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * DAMAGE_FACTOR;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BLAST_REACH), target -> target != owner && !target.is(EntityTypeTags.UNDEAD))) {
            victim.hurtServer(level, level.damageSources().indirectMagic(this, owner), damage);
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS, 0));
        }
        RandomSource random = this.getRandom();
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.HOSTILE, SOUND_VOLUME,
                SOUND_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * SOUND_PITCH_SPREAD);
        this.discard();
    }
}
