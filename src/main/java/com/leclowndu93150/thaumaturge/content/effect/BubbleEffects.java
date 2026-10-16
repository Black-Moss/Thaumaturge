package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class BubbleEffects {
    private static final int SPLASH_COLOR = 0x286176;
    private static final float SPLASH_ALPHA = 0.5F;
    private static final float SPLASH_SCALE_BASE = 0.4F;
    private static final float SPLASH_SCALE_SPAN = 0.3F;
    private static final double SPLASH_OFFSET = 0.075;
    private static final double SPLASH_LAUNCH_BASE = 0.075;
    private static final double SPLASH_LAUNCH_SPREAD = 0.05;
    private static final double SPLASH_SCATTER = 0.015;
    private static final float SPLASH_GRAVITY = 0.3F;
    private static final int SPLASH_AGE_BASE = 20;
    private static final int SPLASH_AGE_BOUND = 10;

    private static final float DROP_SCALE_BASE = 0.4F;
    private static final float DROP_SCALE_SPAN = 0.2F;
    private static final int DROP_AGE_BASE = 20;
    private static final int DROP_AGE_BOUND = 10;
    private static final float DROP_BUOYANCY = 0.01F;
    private static final double DROP_SCATTER = 0.005;

    private static final float FUME_ALPHA = 0.3F;
    private static final double FUME_RISE = 0.012;
    private static final float FUME_BUOYANCY = -0.05F;

    private static final EffectColor FROTH_COLOR = new EffectColor(0.5F, 0.5F, 0.7F);
    private static final float FROTH_ALPHA = 1.0F;
    private static final float FROTH_SCALE_BASE = 0.2F;
    private static final float FROTH_SCALE_SPAN = 0.2F;
    private static final float FROTH_SINK = 0.1F;
    private static final int FROTH_AGE_BASE = 4;
    private static final int FROTH_AGE_BOUND = 3;

    private static final EffectColor FROTH_DOWN_COLOR = new EffectColor(0.25F, 0.0F, 0.75F);
    private static final float FROTH_DOWN_ALPHA = 0.8F;
    private static final float FROTH_DOWN_SCALE_BASE = 0.4F;
    private static final float FROTH_DOWN_SCALE_SPAN = 0.2F;
    private static final float FROTH_DOWN_GRAVITY = 0.05F;
    private static final int FROTH_DOWN_AGE_BASE = 12;
    private static final int FROTH_DOWN_AGE_BOUND = 12;

    private static final int BOIL_BUBBLES = 2;
    private static final double BOIL_OFFSET_BASE = 0.2;
    private static final double BOIL_OFFSET_SPAN = 0.6;
    private static final float BOIL_ALPHA = 1.0F;
    private static final float BOIL_SCALE_BASE = 0.2F;
    private static final float BOIL_SCALE_SPAN = 0.3F;
    private static final int BOIL_AGE_MIN = 7;
    private static final double BOIL_AGE_SPAN = 8.0;
    private static final double BOIL_AGE_SKEW = 0.8;
    private static final double BOIL_AGE_FLOOR = 0.2;
    private static final double BOIL_KICK = 0.002;
    private static final float BOIL_LIFT_PER_HEAT = 0.025F;

    private static final float BUBBLE_ALPHA = 1.0F;
    private static final float BUBBLE_SCALE_BASE = 0.3F;
    private static final float BUBBLE_SCALE_SPAN = 0.3F;
    private static final float BUBBLE_BUOYANCY = -0.001F;
    private static final int BUBBLE_AGE_BASE = 15;
    private static final int BUBBLE_AGE_BOUND = 10;

    private BubbleEffects() {}

    static void jarSplash(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        double x = pos.x + random.nextGaussian() * SPLASH_OFFSET;
        double z = pos.z + random.nextGaussian() * SPLASH_OFFSET;
        double launch = SPLASH_LAUNCH_BASE + random.nextDouble() * SPLASH_LAUNCH_SPREAD;
        float scale = SPLASH_SCALE_BASE + random.nextFloat() * SPLASH_SCALE_SPAN;
        int age = SPLASH_AGE_BASE + random.nextInt(SPLASH_AGE_BOUND);
        BubbleParticleOptions options = new BubbleParticleOptions(EffectColor.opaque(SPLASH_COLOR), SPLASH_ALPHA, scale, age, SPLASH_GRAVITY, true);
        Effects.spawn(level, options, x, pos.y, z, random.nextGaussian() * SPLASH_SCATTER, launch, random.nextGaussian() * SPLASH_SCATTER);
    }

    static void essentiaDrop(ServerLevel level, Vec3 pos, EffectColor color, float alpha) {
        RandomSource random = level.getRandom();
        float scale = DROP_SCALE_BASE + random.nextFloat() * DROP_SCALE_SPAN;
        int age = DROP_AGE_BASE + random.nextInt(DROP_AGE_BOUND);
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), alpha, scale, age, DROP_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, random.nextGaussian() * DROP_SCATTER, random.nextGaussian() * DROP_SCATTER, random.nextGaussian() * DROP_SCATTER);
    }

    static void fluxFume(ServerLevel level, Vec3 pos, EffectColor color, float scale, int maxAge) {
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), FUME_ALPHA, scale, maxAge, FUME_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, 0.0, FUME_RISE, 0.0);
    }

    static void frothDown(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        int age = FROTH_DOWN_AGE_BASE + random.nextInt(FROTH_DOWN_AGE_BOUND);
        float scale = FROTH_DOWN_SCALE_BASE + random.nextFloat() * FROTH_DOWN_SCALE_SPAN;
        BubbleParticleOptions options = new BubbleParticleOptions(FROTH_DOWN_COLOR.argb(), FROTH_DOWN_ALPHA, scale, age, FROTH_DOWN_GRAVITY, true);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }

    static void froth(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        int age = FROTH_AGE_BASE + random.nextInt(FROTH_AGE_BOUND);
        float scale = FROTH_SCALE_BASE + random.nextFloat() * FROTH_SCALE_SPAN;
        BubbleParticleOptions options = new BubbleParticleOptions(FROTH_COLOR.argb(), FROTH_ALPHA, scale, age, FROTH_SINK, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    static void boil(ServerLevel level, Vec3 pos, EffectColor color, int heat) {
        RandomSource random = level.getRandom();
        float buoyancy = -BOIL_LIFT_PER_HEAT * heat;
        for (int i = 0; i < BOIL_BUBBLES; i++) {
            double x = pos.x + BOIL_OFFSET_BASE + random.nextDouble() * BOIL_OFFSET_SPAN;
            double z = pos.z + BOIL_OFFSET_BASE + random.nextDouble() * BOIL_OFFSET_SPAN;
            float scale = BOIL_SCALE_BASE + random.nextFloat() * BOIL_SCALE_SPAN;
            int age = (int) (BOIL_AGE_MIN + BOIL_AGE_SPAN / (BOIL_AGE_SKEW * random.nextDouble() + BOIL_AGE_FLOOR));
            BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), BOIL_ALPHA, scale, age, buoyancy, false);
            Effects.spawn(level, options, x, pos.y, z, 0.0, BOIL_KICK, 0.0);
        }
    }

    static void bubble(ServerLevel level, Vec3 pos, EffectColor color) {
        RandomSource random = level.getRandom();
        int age = BUBBLE_AGE_BASE + random.nextInt(BUBBLE_AGE_BOUND);
        float scale = BUBBLE_SCALE_BASE + random.nextFloat() * BUBBLE_SCALE_SPAN;
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), BUBBLE_ALPHA, scale, age, BUBBLE_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }
}
