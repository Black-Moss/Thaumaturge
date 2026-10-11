package com.leclowndu93150.thaumaturge.content.entity.boss;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
    private static final float SPEED_DIVISOR = 40.0F;
    private static final int PUFF_ODDS = 20;
    private static final double PUFF_LIFT = 0.5;
    private static final double PUFF_SPREAD = 0.3;
    private static final int PUFF_COUNT = 1;
    private static final double PUFF_SPEED = 0.0;

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
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, owner.getX(), owner.getY() + owner.getBbHeight() + PUFF_LIFT, owner.getZ(), PUFF_COUNT, PUFF_SPREAD, PUFF_SPREAD, PUFF_SPREAD,
                    PUFF_SPEED);
        }
    }

    private void erupt(DamageSource source, float damage) {
        setAnger(RAGE_TICKS);
        empower(MobEffects.REGENERATION, damage / REGENERATION_DIVISOR);
        empower(MobEffects.STRENGTH, damage / STRENGTH_DIVISOR);
        empower(MobEffects.SPEED, damage / SPEED_DIVISOR);
        if (source.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable(ENRAGED_KEY, owner.getDisplayName()), true);
        }
    }

    private void empower(Holder<MobEffect> effect, float scaled) {
        owner.addEffect(new MobEffectInstance(effect, RAGE_TICKS, Mth.floor(scaled)));
    }
}
