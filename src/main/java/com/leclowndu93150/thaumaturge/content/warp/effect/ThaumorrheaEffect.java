package com.leclowndu93150.thaumaturge.content.warp.effect;

import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class ThaumorrheaEffect extends MobEffect {
    private static final int COLOR = 0xB300B3;
    private static final int PULSE_INTERVAL = 20;
    private static final int GOO_ODDS = 15;
    private static final int GOO_QUANTA = 1;

    public ThaumorrheaEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % PULSE_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        if (mob.getRandom().nextInt(GOO_ODDS) != 0) {
            return true;
        }
        BlockPos pos = mob.blockPosition();
        if (level.getBlockState(pos).isAir()) {
            PhysicalFlux.placeGoo(level, pos, GOO_QUANTA);
        }
        return true;
    }
}
