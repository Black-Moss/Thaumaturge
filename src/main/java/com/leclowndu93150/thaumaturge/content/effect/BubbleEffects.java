package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class BubbleEffects {
    private static final int JAR_COLOR = 0x286176;
    private static final float JAR_ALPHA = 0.5F;
    private static final float JAR_SCALE_BASE = 0.4F;
    private static final float JAR_SCALE_SPREAD = 0.3F;
    private static final int JAR_AGE_BASE = 20;
    private static final int JAR_AGE_RANGE = 10;
    private static final float JAR_BUOYANCY = 0.3F;
    private static final double JAR_POSITION_DEVIATION = 0.075;
    private static final double JAR_DRIFT_DEVIATION = 0.015;
    private static final double JAR_RISE_BASE = 0.075;
    private static final double JAR_RISE_SPREAD = 0.05;
    private static final float DROP_SCALE_BASE = 0.4F;
    private static final float DROP_SCALE_SPREAD = 0.2F;
    private static final int DROP_AGE_BASE = 20;
    private static final int DROP_AGE_RANGE = 10;
    private static final float DROP_BUOYANCY = 0.01F;
    private static final double DROP_DRIFT_DEVIATION = 0.005;
    private static final float FUME_ALPHA = 0.25F;
    private static final float FUME_BUOYANCY = -0.01F;
    private static final EffectColor FROTH_DOWN_COLOR = new EffectColor(0.25F, 0.0F, 0.75F);
    private static final float FROTH_DOWN_ALPHA = 0.8F;
    private static final float FROTH_DOWN_SCALE_BASE = 0.4F;
    private static final float FROTH_DOWN_SCALE_SPREAD = 0.2F;
    private static final int FROTH_DOWN_AGE_BASE = 12;
    private static final int FROTH_DOWN_AGE_RANGE = 12;
    private static final float FROTH_DOWN_BUOYANCY = 0.05F;
    private static final EffectColor FROTH_COLOR = new EffectColor(0.5F, 0.5F, 0.7F);
    private static final float FROTH_SCALE_BASE = 0.2F;
    private static final float FROTH_SCALE_SPREAD = 0.2F;
    private static final int FROTH_AGE_BASE = 4;
    private static final int FROTH_AGE_RANGE = 3;
    private static final float FROTH_BUOYANCY = 0.1F;
    private static final int BOIL_BUBBLES = 2;
    private static final float BOIL_SCALE_BASE = 0.2F;
    private static final float BOIL_SCALE_SPREAD = 0.3F;
    private static final float BOIL_AGE_BASE = 7.0F;
    private static final float BOIL_AGE_NUMERATOR = 8.0F;
    private static final float BOIL_AGE_DIVISOR_SPREAD = 0.8F;
    private static final float BOIL_AGE_DIVISOR_FLOOR = 0.2F;
    private static final float BOIL_BUOYANCY_PER_HEAT = -0.025F;
    private static final double BOIL_INSET = 0.2;
    private static final double BOIL_SPAN = 0.6;
    private static final double BOIL_RISE = 0.002;
    private static final float BUBBLE_SCALE_BASE = 0.3F;
    private static final float BUBBLE_SCALE_SPREAD = 0.3F;
    private static final int BUBBLE_AGE_BASE = 15;
    private static final int BUBBLE_AGE_RANGE = 10;
    private static final float BUBBLE_BUOYANCY = -0.001F;
    private static final float FULL_ALPHA = 1.0F;

    private BubbleEffects() {}

    static void jarSplash(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(JAR_COLOR, JAR_ALPHA, JAR_SCALE_BASE + random.nextFloat() * JAR_SCALE_SPREAD, JAR_AGE_BASE + random.nextInt(JAR_AGE_RANGE),
                JAR_BUOYANCY, true);
        Effects.spawn(level, options, pos.x + random.nextGaussian() * JAR_POSITION_DEVIATION, pos.y, pos.z + random.nextGaussian() * JAR_POSITION_DEVIATION,
                random.nextGaussian() * JAR_DRIFT_DEVIATION, JAR_RISE_BASE + random.nextDouble() * JAR_RISE_SPREAD, random.nextGaussian() * JAR_DRIFT_DEVIATION);
    }

    static void essentiaDrop(ServerLevel level, Vec3 pos, EffectColor color, float alpha) {
        RandomSource random = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), alpha, DROP_SCALE_BASE + random.nextFloat() * DROP_SCALE_SPREAD, DROP_AGE_BASE + random.nextInt(DROP_AGE_RANGE),
                DROP_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, random.nextGaussian() * DROP_DRIFT_DEVIATION, random.nextGaussian() * DROP_DRIFT_DEVIATION, random.nextGaussian() * DROP_DRIFT_DEVIATION);
    }

    static void fluxFume(ServerLevel level, Vec3 pos, EffectColor color, float scale, int maxAge) {
        Effects.spawn(level, new BubbleParticleOptions(color.argb(), FUME_ALPHA, scale, maxAge, FUME_BUOYANCY, false), pos.x, pos.y, pos.z);
    }

    static void frothDown(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(FROTH_DOWN_COLOR.argb(), FROTH_DOWN_ALPHA, FROTH_DOWN_SCALE_BASE + random.nextFloat() * FROTH_DOWN_SCALE_SPREAD,
                FROTH_DOWN_AGE_BASE + random.nextInt(FROTH_DOWN_AGE_RANGE), FROTH_DOWN_BUOYANCY, true);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }

    static void froth(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(FROTH_COLOR.argb(), FULL_ALPHA, FROTH_SCALE_BASE + random.nextFloat() * FROTH_SCALE_SPREAD,
                FROTH_AGE_BASE + random.nextInt(FROTH_AGE_RANGE), FROTH_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }

    static void boil(ServerLevel level, Vec3 pos, EffectColor color, int heat) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < BOIL_BUBBLES; i++) {
            int age = (int) (BOIL_AGE_BASE + BOIL_AGE_NUMERATOR / (random.nextFloat() * BOIL_AGE_DIVISOR_SPREAD + BOIL_AGE_DIVISOR_FLOOR));
            BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), FULL_ALPHA, BOIL_SCALE_BASE + random.nextFloat() * BOIL_SCALE_SPREAD, age, BOIL_BUOYANCY_PER_HEAT * heat, false);
            Effects.spawn(level, options, pos.x + BOIL_INSET + random.nextDouble() * BOIL_SPAN, pos.y, pos.z + BOIL_INSET + random.nextDouble() * BOIL_SPAN, 0.0, BOIL_RISE, 0.0);
        }
    }

    static void bubble(ServerLevel level, Vec3 pos, EffectColor color) {
        RandomSource random = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), FULL_ALPHA, BUBBLE_SCALE_BASE + random.nextFloat() * BUBBLE_SCALE_SPREAD,
                BUBBLE_AGE_BASE + random.nextInt(BUBBLE_AGE_RANGE), BUBBLE_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }
}
