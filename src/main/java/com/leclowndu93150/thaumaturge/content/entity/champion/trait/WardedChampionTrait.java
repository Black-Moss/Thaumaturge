package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.content.entity.champion.ShieldChargeSound;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

public final class WardedChampionTrait extends AbstractChampionTrait {
    private static final int WARD_DIVISOR = 2;
    private static final int REGENERATION_INTERVAL = 25;
    private static final float REGENERATION_AMOUNT = 1.0F;

    @Override
    protected void championModifiers(LivingEntity mob, MobTraitModifiers modifiers) {
        float ward = ward(mob);
        double ceiling = mob.getAttributeBaseValue(Attributes.MAX_ABSORPTION);
        if (ward <= ceiling) {
            return;
        }
        modifiers.setBase(Attributes.MAX_ABSORPTION, ward);
    }

    @Override
    protected void onChampionAdded(LivingEntity mob) {
        float granted = ward(mob);
        mob.setAbsorptionAmount(granted + mob.getAbsorptionAmount());
    }

    @Override
    public void tick(LivingEntity mob) {
        boolean ready = mob.invulnerableTime <= 0 && mob.tickCount % REGENERATION_INTERVAL == 0;
        float current = mob.getAbsorptionAmount();
        if (ready && current < ward(mob)) {
            mob.setAbsorptionAmount(current + REGENERATION_AMOUNT);
        }
    }

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        ShieldChargeSound.playIfShielded(mob);
        return amount;
    }

    private static float ward(LivingEntity mob) {
        return (int) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / WARD_DIVISOR;
    }
}
