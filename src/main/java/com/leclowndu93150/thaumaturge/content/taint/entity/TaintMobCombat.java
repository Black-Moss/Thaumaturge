package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class TaintMobCombat {
    private static final int NORMAL_SEVERITY = 3;
    private static final int HARD_SEVERITY = 6;
    private static final int MIN_ROLL = 2;
    private static final int TICKS_PER_SEVERITY = 20;

    private TaintMobCombat() {}

    public static void onHit(Mob attacker, Entity target, boolean hit) {
        if (hit && target instanceof LivingEntity victim) {
            maybeApplyFluxTaint(attacker, victim);
        }
    }

    public static void maybeApplyFluxTaint(Mob attacker, LivingEntity victim) {
        int severity = switch (attacker.level().getDifficulty()) {
            case NORMAL -> NORMAL_SEVERITY;
            case HARD -> HARD_SEVERITY;
            default -> 0;
        };
        if (severity > 0 && attacker.getRandom().nextInt(severity + 1) > MIN_ROLL) {
            victim.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, severity * TICKS_PER_SEVERITY, 0));
        }
    }
}
