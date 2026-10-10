package com.leclowndu93150.thaumaturge.content.taint.effect;

import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class InfectiousVisExhaustEffect extends MobEffect {
    private static final int COLOR = 0x6A3D7A;
    private static final int SPREAD_PERIOD = 40;
    private static final double SPREAD_RADIUS = 4.0;
    private static final int PASSED_DURATION = 6000;
    private static final boolean PASSED_AMBIENT = false;
    private static final boolean PASSED_PARTICLES = true;
    private static final boolean PASSED_ICON = false;

    public InfectiousVisExhaustEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % SPREAD_PERIOD == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity carrier, int amplification) {
        Holder<MobEffect> passed = amplification > 0 ? TTMobEffects.INFECTIOUS_VIS_EXHAUST : TTMobEffects.VIS_EXHAUST;
        int passedAmplifier = Math.max(0, amplification - 1);
        for (LivingEntity neighbour : level.getEntitiesOfClass(LivingEntity.class, carrier.getBoundingBox().inflate(SPREAD_RADIUS))) {
            if (neighbour != carrier && !neighbour.hasEffect(TTMobEffects.INFECTIOUS_VIS_EXHAUST)) {
                neighbour.addEffect(new MobEffectInstance(passed, PASSED_DURATION, passedAmplifier, PASSED_AMBIENT, PASSED_PARTICLES, PASSED_ICON), carrier);
            }
        }
        return true;
    }
}
