package com.leclowndu93150.thaumaturge.content.entity.boss;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

final class BossRage {
    private static final String ENRAGED_KEY = "message.thaumaturge.boss.enraged";
    private static final float HIT_CAP = 35.0F;
    private static final int RAGE_TICKS = 200;
    private static final float REGENERATION_DIVISOR = 15.0F;
    private static final float STRENGTH_DIVISOR = 10.0F;
    private static final float HASTE_DIVISOR = 40.0F;
    private static final int PUFF_ODDS = 15;
    private static final float PUFF_LIFT = 0.5F;
    private static final double PUFF_DRIFT = 0.02;
    private static final int PUFF_VELOCITY_MODE = 0;
    private static final double PUFF_SPEED = 1.0;

    private final LivingEntity owner;
    private final EntityDataAccessor<Integer> anger;

    BossRage(LivingEntity owner, EntityDataAccessor<Integer> anger) {
        this.owner = owner;
        this.anger = anger;
    }

    int anger() {
        return owner.getEntityData().get(anger);
    }

    void setAnger(int ticks) {
        owner.getEntityData().set(anger, Math.max(0, ticks));
    }

    boolean enraged() {
        return anger() > 0;
    }

    float absorb(DamageSource source, float damage) {
        if (damage <= HIT_CAP || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        if (!enraged() && owner.level() instanceof ServerLevel level && !owner.isInvulnerableTo(level, source) && owner.isAlive()) {
            erupt(source, damage);
        }
        return HIT_CAP;
    }

    void tick() {
        if (!(owner.level() instanceof ServerLevel level)) {
            return;
        }
        int remaining = anger();
        if (remaining <= 0) {
            return;
        }
        setAnger(remaining - 1);
        if (owner.getRandom().nextInt(PUFF_ODDS) == 0) {
            puff(level);
        }
    }

    private void puff(ServerLevel level) {
        RandomSource random = owner.getRandom();
        float width = owner.getBbWidth();
        double x = owner.getX() + random.nextFloat() * width - width / 2.0F;
        double y = owner.getY() + owner.getBbHeight() + random.nextFloat() * PUFF_LIFT;
        double z = owner.getZ() + random.nextFloat() * width - width / 2.0F;
        double driftX = random.nextGaussian() * PUFF_DRIFT;
        double driftY = random.nextGaussian() * PUFF_DRIFT;
        double driftZ = random.nextGaussian() * PUFF_DRIFT;
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, PUFF_VELOCITY_MODE, driftX, driftY, driftZ, PUFF_SPEED);
    }

    private void erupt(DamageSource source, float damage) {
        setAnger(RAGE_TICKS);
        empower(MobEffects.REGENERATION, damage / REGENERATION_DIVISOR);
        empower(MobEffects.STRENGTH, damage / STRENGTH_DIVISOR);
        empower(MobEffects.HASTE, damage / HASTE_DIVISOR);
        if (source.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable(ENRAGED_KEY, owner.getDisplayName()), true);
        }
    }

    private void empower(Holder<MobEffect> effect, float scaled) {
        owner.addEffect(new MobEffectInstance(effect, RAGE_TICKS, Mth.floor(scaled)));
    }
}
