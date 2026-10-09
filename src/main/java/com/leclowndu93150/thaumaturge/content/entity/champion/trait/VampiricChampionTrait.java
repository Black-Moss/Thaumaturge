package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class VampiricChampionTrait extends AbstractChampionTrait {
    private static final float MINIMUM_HEAL = 2.0F;
    private static final float DAMAGE_SHARE = 0.5F;

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        mob.heal(Math.max(MINIMUM_HEAL, amount * DAMAGE_SHARE));
        return amount;
    }
}
