package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class FieryChampionTrait extends AbstractChampionTrait {
    private static final float IGNITE_CHANCE = 0.4F;
    private static final float IGNITE_SECONDS = 4.0F;

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < IGNITE_CHANCE) {
            target.igniteForSeconds(IGNITE_SECONDS);
        }
        return amount;
    }
}
