package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

final class BossRage {
    private static final String ENRAGED_KEY = "message.thaumaturge.boss.enraged";
    private static final float HIT_CAP = 35.0F;
    private static final int ANGER_TICKS = 200;
    private static final int BUFF_TICKS = 200;
    private static final float REGENERATION_DIVISOR = 15.0F;
    private static final float STRENGTH_DIVISOR = 10.0F;
    private static final float HASTE_DIVISOR = 40.0F;
    private static final int MAX_AMPLIFIER = 255;
    private static final int PUFF_ONE_IN = 15;
    private static final double PUFF_DRIFT = 0.02;
    private static final double PUFF_SPREAD = 0.5;

    private final LivingEntity boss;
    private final EntityDataAccessor<Integer> anger;

    BossRage(LivingEntity boss, EntityDataAccessor<Integer> anger) {
        this.boss = boss;
        this.anger = anger;
    }

    int anger() {
        return boss.getEntityData().get(anger);
    }

    void setAnger(int value) {
        boss.getEntityData().set(anger, value);
    }

    void tick() {
        int current = anger();
        if (current <= 0) {
            return;
        }
        if (boss.level().isClientSide()) {
            if (boss.getRandom().nextInt(PUFF_ONE_IN) == 0) {
                puff();
            }
        } else {
            setAnger(current - 1);
        }
    }

    float absorb(DamageSource source, float damage) {
        if (damage <= HIT_CAP || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        if (anger() <= 0) {
            enrage(source, damage);
        }
        return HIT_CAP;
    }

    private void enrage(DamageSource source, float damage) {
        buff(MobEffects.REGENERATION, damage / REGENERATION_DIVISOR);
        buff(MobEffects.STRENGTH, damage / STRENGTH_DIVISOR);
        buff(MobEffects.HASTE, damage / HASTE_DIVISOR);
        setAnger(ANGER_TICKS);
        if (source.getEntity() instanceof Player player) {
            TTActionBar.send(player, Component.translatable(ENRAGED_KEY, boss.getDisplayName()));
        }
    }

    private void buff(Holder<MobEffect> effect, float level) {
        boss.addEffect(new MobEffectInstance(effect, BUFF_TICKS, Mth.clamp((int) level, 0, MAX_AMPLIFIER)));
    }

    private void puff() {
        RandomSource random = boss.getRandom();
        boss.level().addParticle(ParticleTypes.ANGRY_VILLAGER, boss.getRandomX(PUFF_SPREAD), boss.getY() + boss.getBbHeight(), boss.getRandomZ(PUFF_SPREAD), random.nextGaussian() * PUFF_DRIFT,
                random.nextGaussian() * PUFF_DRIFT, random.nextGaussian() * PUFF_DRIFT);
    }
}
