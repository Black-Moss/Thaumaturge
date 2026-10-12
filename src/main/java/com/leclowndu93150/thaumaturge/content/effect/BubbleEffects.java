package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EssentiaDropParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class BubbleEffects {
    private static final int SPLASH_COLOR = 0x1C6E7A;
    private static final float SPLASH_ALPHA = 0.5F;
    private static final float SPLASH_SCALE = 0.8F;
    private static final double SPLASH_OFFSET = 0.06;
    private static final double SPLASH_LAUNCH_BASE = 0.1;
    private static final double SPLASH_LAUNCH_SPREAD = 0.06;
    private static final double SPLASH_SCATTER = 0.025;
    private static final float SPLASH_GRAVITY = 0.6F;
    private static final int SPLASH_AGE_BASE = 20;
    private static final int SPLASH_AGE_BOUND = 11;

    private static final float FUME_ALPHA = 0.3F;
    private static final double FUME_RISE = 0.012;
    private static final float FUME_BUOYANCY = -0.05F;

    private static final int FROTH_COLOR = 0xCBC2DA;
    private static final float FROTH_ALPHA = 0.7F;
    private static final float FROTH_SCALE = 0.45F;
    private static final double FROTH_PUSH_DOWN = -0.006;
    private static final float FROTH_SINK = 0.1F;
    private static final int FROTH_AGE_BASE = 3;
    private static final int FROTH_AGE_BOUND = 5;

    private static final int FROTH_DOWN_COLOR = 0x3B2470;
    private static final float FROTH_DOWN_ALPHA = 0.85F;
    private static final float FROTH_DOWN_SCALE = 1.3F;
    private static final float FROTH_DOWN_GRAVITY = 0.35F;
    private static final int FROTH_DOWN_AGE_BASE = 10;
    private static final int FROTH_DOWN_AGE_BOUND = 11;

    private static final int BOIL_BUBBLES = 2;
    private static final double BOIL_SPREAD = 0.3;
    private static final float BOIL_ALPHA = 0.8F;
    private static final float BOIL_SCALE = 0.9F;
    private static final int BOIL_AGE_MIN = 7;
    private static final int BOIL_AGE_SPAN = 24;
    private static final double BOIL_KICK_PER_HEAT = 0.01;
    private static final float BOIL_LIFT_PER_HEAT = 0.006F;

    private static final float BUBBLE_ALPHA = 0.8F;
    private static final float BUBBLE_SCALE = 1.0F;
    private static final float BUBBLE_BUOYANCY = -0.01F;
    private static final int BUBBLE_AGE_BASE = 18;
    private static final int BUBBLE_AGE_BOUND = 5;

    private BubbleEffects() {}

    static void jarSplash(ServerLevel level, Vec3 pos) {
        RandomSource random = level.getRandom();
        double x = pos.x + random.nextGaussian() * SPLASH_OFFSET;
        double y = pos.y + random.nextGaussian() * SPLASH_OFFSET;
        double z = pos.z + random.nextGaussian() * SPLASH_OFFSET;
        double launch = SPLASH_LAUNCH_BASE + random.nextDouble() * SPLASH_LAUNCH_SPREAD;
        int age = SPLASH_AGE_BASE + random.nextInt(SPLASH_AGE_BOUND);
        BubbleParticleOptions options = new BubbleParticleOptions(EffectColor.opaque(SPLASH_COLOR), SPLASH_ALPHA, SPLASH_SCALE, age, SPLASH_GRAVITY, true);
        Effects.spawn(level, options, x, y, z, random.nextGaussian() * SPLASH_SCATTER, launch, random.nextGaussian() * SPLASH_SCATTER);
    }

    static void essentiaDrop(ServerLevel level, Vec3 pos, EffectColor color, float alpha) {
        Effects.spawn(level, new EssentiaDropParticleOptions(color.argb(), alpha), pos.x, pos.y, pos.z);
    }

    static void fluxFume(ServerLevel level, Vec3 pos, EffectColor color, float scale, int maxAge) {
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), FUME_ALPHA, scale, maxAge, FUME_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, 0.0, FUME_RISE, 0.0);
    }

    static void frothDown(ServerLevel level, Vec3 pos) {
        int age = FROTH_DOWN_AGE_BASE + level.getRandom().nextInt(FROTH_DOWN_AGE_BOUND);
        BubbleParticleOptions options = new BubbleParticleOptions(EffectColor.opaque(FROTH_DOWN_COLOR), FROTH_DOWN_ALPHA, FROTH_DOWN_SCALE, age, FROTH_DOWN_GRAVITY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }

    static void froth(ServerLevel level, Vec3 pos) {
        int age = FROTH_AGE_BASE + level.getRandom().nextInt(FROTH_AGE_BOUND);
        BubbleParticleOptions options = new BubbleParticleOptions(EffectColor.opaque(FROTH_COLOR), FROTH_ALPHA, FROTH_SCALE, age, FROTH_SINK, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z, 0.0, FROTH_PUSH_DOWN, 0.0);
    }

    static void boil(ServerLevel level, Vec3 pos, EffectColor color, int heat) {
        RandomSource random = level.getRandom();
        float buoyancy = -BOIL_LIFT_PER_HEAT * heat;
        double kick = BOIL_KICK_PER_HEAT * heat;
        for (int i = 0; i < BOIL_BUBBLES; i++) {
            double x = pos.x + (random.nextDouble() * 2.0 - 1.0) * BOIL_SPREAD;
            double z = pos.z + (random.nextDouble() * 2.0 - 1.0) * BOIL_SPREAD;
            float skew = random.nextFloat();
            int age = BOIL_AGE_MIN + (int) (skew * skew * BOIL_AGE_SPAN);
            BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), BOIL_ALPHA, BOIL_SCALE, age, buoyancy, false);
            Effects.spawn(level, options, x, pos.y, z, 0.0, kick, 0.0);
        }
    }

    static void bubble(ServerLevel level, Vec3 pos, EffectColor color) {
        int age = BUBBLE_AGE_BASE + level.getRandom().nextInt(BUBBLE_AGE_BOUND);
        BubbleParticleOptions options = new BubbleParticleOptions(color.argb(), BUBBLE_ALPHA, BUBBLE_SCALE, age, BUBBLE_BUOYANCY, false);
        Effects.spawn(level, options, pos.x, pos.y, pos.z);
    }
}
