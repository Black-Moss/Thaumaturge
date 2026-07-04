package com.leclowndu93150.thaumcraft.content.warp.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class UnnaturalHungerEffect extends MobEffect {
    private static final float EXHAUSTION_PER_LEVEL = 0.025F;

    public UnnaturalHungerEffect() {
        super(MobEffectCategory.HARMFUL, 0x2B1D0E);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        if (mob instanceof Player player) {
            player.causeFoodExhaustion(EXHAUSTION_PER_LEVEL * (amplification + 1));
        }
        return true;
    }
}
