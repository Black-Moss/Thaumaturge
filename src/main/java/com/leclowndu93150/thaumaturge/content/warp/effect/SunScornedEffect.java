package com.leclowndu93150.thaumaturge.content.warp.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class SunScornedEffect extends MobEffect {
    private static final int COLOR = 0xFFDD55;
    private static final int PULSE_INTERVAL = 40;
    private static final float MAX_LIGHT = 15.0F;
    private static final float BURN_THRESHOLD = 0.5F;
    private static final float BURN_SECONDS = 4.0F;
    private static final float HEAL_THRESHOLD = 0.25F;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final int UNLOADED_LIGHT = 0;
    private static final float SHADE_FALLOFF = 3.0F;
    private static final float FULL_BRIGHTNESS = 1.0F;
    private static final float KINDLING_BRIGHTNESS = 0.4F;
    private static final float FULL_LIGHT_BURN_CHANCE = 0.04F;
    private static final float HEAL_FADE_BRIGHTNESS = 0.5F;

    public SunScornedEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % PULSE_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        BlockPos eyes = BlockPos.containing(mob.getX(), mob.getEyeY(), mob.getZ());
        float brightness = shadedBrightness(level, eyes);
        RandomSource random = mob.getRandom();
        if (brightness > BURN_THRESHOLD) {
            if (level.canSeeSky(eyes) && random.nextFloat() < burnChance(brightness)) {
                mob.igniteForSeconds(BURN_SECONDS);
            }
        } else if (brightness < HEAL_THRESHOLD && random.nextFloat() < healChance(brightness)) {
            mob.heal(HEAL_AMOUNT);
        }
        return true;
    }

    private static float shadedBrightness(ServerLevel level, BlockPos eyes) {
        int light = level.hasChunkAt(eyes) ? level.getMaxLocalRawBrightness(eyes) : UNLOADED_LIGHT;
        float fraction = light / MAX_LIGHT;
        float shaded = fraction / (FULL_BRIGHTNESS + SHADE_FALLOFF * (FULL_BRIGHTNESS - fraction));
        return Mth.lerp(level.dimensionType().ambientLight(), shaded, FULL_BRIGHTNESS);
    }

    private static float burnChance(float brightness) {
        return FULL_LIGHT_BURN_CHANCE * (brightness - KINDLING_BRIGHTNESS) / (FULL_BRIGHTNESS - KINDLING_BRIGHTNESS);
    }

    private static float healChance(float brightness) {
        return FULL_BRIGHTNESS - brightness / HEAL_FADE_BRIGHTNESS;
    }
}
