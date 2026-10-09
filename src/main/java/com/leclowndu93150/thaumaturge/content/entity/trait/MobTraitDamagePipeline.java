package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class MobTraitDamagePipeline {
    private MobTraitDamagePipeline() {}

    public static void apply(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide()) {
            event.setAmount(run(target, event.getSource(), event.getAmount()));
        }
    }

    private static float run(LivingEntity target, DamageSource cause, float incoming) {
        LivingEntity dealer = cause.getEntity() instanceof LivingEntity living ? living : null;
        float amount = incoming;
        List<Holder<MobTrait>> guards = MobTraitEngine.traits(target);
        for (int i = 0; i < guards.size(); i++) {
            amount = guards.get(i).value().onHurt(target, dealer, cause, amount);
        }
        if (dealer == null || amount <= 0.0F) {
            return amount;
        }
        for (Holder<MobTrait> trait : MobTraitEngine.traits(dealer)) {
            amount = trait.value().onAttack(dealer, target, cause, amount);
        }
        return amount;
    }
}
