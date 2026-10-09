package com.leclowndu93150.thaumaturge.content.taint.effect;

import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class InfectiousVisExhaustEffect extends MobEffect {
    private static final int COLOR = 0x60306F;
    private static final int PERIOD = 40;
    private static final double RANGE = 4.0;
    private static final int SPREAD_DURATION = 6000;

    public InfectiousVisExhaustEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % PERIOD == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        boolean weakest = amplification <= 0;
        Holder<MobEffect> spread = weakest ? TTMobEffects.VIS_EXHAUST : TTMobEffects.INFECTIOUS_VIS_EXHAUST;
        int spreadAmplifier = weakest ? 0 : amplification - 1;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(RANGE))) {
            if (other != mob && !other.hasEffect(TTMobEffects.INFECTIOUS_VIS_EXHAUST)) {
                other.addEffect(new MobEffectInstance(spread, SPREAD_DURATION, spreadAmplifier, false, true, false));
            }
        }
        return true;
    }
}
