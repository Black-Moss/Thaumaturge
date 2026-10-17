package com.leclowndu93150.thaumaturge.content.warp.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class SunScornedEffect extends MobEffect {
    private static final int COLOR = 0xFFDD55;
    private static final int PULSE_INTERVAL = 40;
    private static final float MAX_LIGHT = 15.0F;
    private static final float CURVE_BASE = 4.0F;
    private static final float CURVE_SLOPE = 3.0F;
    private static final float BURN_THRESHOLD = 0.5F;
    private static final float BURN_OFFSET = 0.4F;
    private static final float BURN_SCALE = 2.0F / 30.0F;
    private static final float BURN_SECONDS = 4.0F;
    private static final float HEAL_THRESHOLD = 0.25F;
    private static final float HEAL_SLOPE = 2.0F;
    private static final float HEAL_AMOUNT = 1.0F;

    public SunScornedEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % PULSE_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        float brightness = brightness(level, mob);
        RandomSource random = mob.getRandom();
        if (brightness > BURN_THRESHOLD) {
            if (level.canSeeSky(mob.blockPosition()) && random.nextFloat() < (brightness - BURN_OFFSET) * BURN_SCALE) {
                mob.igniteForSeconds(BURN_SECONDS);
            }
        } else if (brightness < HEAL_THRESHOLD && random.nextFloat() < 1.0F - HEAL_SLOPE * brightness) {
            mob.heal(HEAL_AMOUNT);
        }
        return true;
    }

    private static float brightness(ServerLevel level, LivingEntity mob) {
        BlockPos eye = BlockPos.containing(mob.getX(), mob.getEyeY(), mob.getZ());
        if (!level.hasChunkAt(eye)) {
            return 0.0F;
        }
        float light = level.getMaxLocalRawBrightness(eye) / MAX_LIGHT;
        float curved = light / (CURVE_BASE - CURVE_SLOPE * light);
        float ambient = level.dimensionType().ambientLight();
        return ambient + (1.0F - ambient) * curved;
    }
}
