package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClientEffects {
    private static final float CHANNEL_MAX = ColorRange.CHANNEL_MAX;
    private static final float OPAQUE = 1.0F;

    private static final float DROP_SCALE_BASE = 0.4F;
    private static final float DROP_SCALE_SPREAD = 0.2F;
    private static final int DROP_AGE_BASE = 20;
    private static final int DROP_AGE_RANGE = 10;
    private static final float DROP_BUOYANCY = 0.01F;
    private static final double DROP_DRIFT = 0.005;

    private static final float FOLLOW_RED = 0.33F;
    private static final float FOLLOW_GREEN = 0.33F;
    private static final float FOLLOW_BLUE = 1.0F;
    private static final float FOLLOW_SCALE_BASE = 0.3F;
    private static final float FOLLOW_SCALE_SPREAD = 0.3F;
    private static final int FOLLOW_AGE_BASE = 15;
    private static final int FOLLOW_AGE_RANGE = 10;
    private static final float FOLLOW_BUOYANCY = -0.001F;
    private static final double FOLLOW_SPREAD = 0.2;

    private static final float FLAME_ALPHA = 0.66F;
    private static final float FLAME_SCALE_BASE = 3.0F;
    private static final float FLAME_END_SCALE = 0.05F;

    private static final double RUNE_CENTRE = 0.5;

    private static final float SCAN_SCALE_BASE = 0.4F;
    private static final float SCAN_SCALE_DEVIATION = 0.1F;
    private static final ColorRange SCAN_RED = new ColorRange(16, 32);
    private static final ColorRange SCAN_GREEN = new ColorRange(132, 165);
    private static final ColorRange SCAN_BLUE = new ColorRange(223, 239);
    private static final int SCAN_DELAY_RANGE = 10;
    private static final float SCAN_DECAY = 1.0F;
    private static final float SCAN_GRAVITY = 0.0F;
    private static final int SCAN_BASE_AGE = 4;

    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BLUE_SHIFT = 0;
    private static final int CHANNEL_MASK = 0xFF;

    private enum BubbleKind {
        DROP(DROP_SCALE_BASE, DROP_SCALE_SPREAD, DROP_AGE_BASE, DROP_AGE_RANGE, DROP_BUOYANCY), FOLLOW(FOLLOW_SCALE_BASE, FOLLOW_SCALE_SPREAD, FOLLOW_AGE_BASE, FOLLOW_AGE_RANGE, FOLLOW_BUOYANCY);

        private final float scaleBase;
        private final float scaleSpread;
        private final int ageBase;
        private final int ageRange;
        private final float buoyancy;

        BubbleKind(float scaleBase, float scaleSpread, int ageBase, int ageRange, float buoyancy) {
            this.scaleBase = scaleBase;
            this.scaleSpread = scaleSpread;
            this.ageBase = ageBase;
            this.ageRange = ageRange;
            this.buoyancy = buoyancy;
        }

        BubbleParticleOptions spawn(RandomSource random, int tint, float alpha) {
            float scale = scaleBase + random.nextFloat() * scaleSpread;
            int age = ageBase + random.nextInt(ageRange);
            return new BubbleParticleOptions(tint, alpha, scale, age, buoyancy, false);
        }
    }

    private ClientEffects() {
        throw new UnsupportedOperationException();
    }

    public static void essentiaDrop(Level level, double x, double y, double z, float r, float g, float b, float alpha) {
        RandomSource random = level.getRandom();
        BubbleParticleOptions options = BubbleKind.DROP.spawn(random, opaque(r, g, b), alpha);
        submit(options, x, y, z, random.nextGaussian() * DROP_DRIFT, random.nextGaussian() * DROP_DRIFT, random.nextGaussian() * DROP_DRIFT);
    }

    public static void spiralSmoke(Level level, double x, double y, double z, float radius, int startAngle, int minY, int color) {
        level.addParticle(new SmokeSpiralParticleOptions(radius, startAngle, minY, channel(color, RED_SHIFT), channel(color, GREEN_SHIFT), channel(color, BLUE_SHIFT)), x, y, z, 0.0, 0.0, 0.0);
    }

    public static void followingBubble(Level level, double x, double y, double z) {
        submit(BubbleKind.FOLLOW.spawn(level.getRandom(), opaque(FOLLOW_RED, FOLLOW_GREEN, FOLLOW_BLUE), OPAQUE), x, y, z, 0.0, 0.0, 0.0);
    }

    public static void followingBubbleAbove(Level level, Entity entity) {
        RandomSource random = level.getRandom();
        double[] jitter = new double[3];
        for (int axis = 0; axis < jitter.length; axis++) {
            jitter[axis] = random.triangle(0.0, FOLLOW_SPREAD);
        }
        Vec3 feet = entity.position();
        double top = feet.y + entity.getBbHeight();
        followingBubble(level, feet.x + jitter[0], top + jitter[1], feet.z + jitter[2]);
    }

    public static void nitorCore(Level level, double x, double y, double z, double vx, double vy, double vz, int color) {
        submit(new NitorCoreParticleOptions(channel(color, RED_SHIFT), channel(color, GREEN_SHIFT), channel(color, BLUE_SHIFT)), x, y, z, vx, vy, vz);
    }

    public static void nitorFlames(Level level, double x, double y, double z, double vx, double vy, double vz, int color, int delay) {
        WispFlameParticleOptions flame = new WispFlameParticleOptions(color, FLAME_ALPHA, level.getRandom().nextFloat() + FLAME_SCALE_BASE, FLAME_END_SCALE, delay);
        submit(flame, x, y, z, vx, vy, vz);
    }

    public static void runeGlyph(Level level, double x, double y, double z, float r, float g, float b, int duration, float gravity) {
        BlockRunesParticleOptions runes = new BlockRunesParticleOptions(r, g, b, duration, gravity, false);
        submit(runes, RUNE_CENTRE + x, RUNE_CENTRE + y, RUNE_CENTRE + z, 0.0, 0.0, 0.0);
    }

    public static void scanSparkles(Level level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }
        AABB bounds = shape.bounds();
        scanSparkles(level, bounds.move(pos));
    }

    public static void scanSparkles(Entity entity) {
        scanSparkles(entity.level(), entity.getBoundingBox());
    }

    public static void scanSparkles(Level level, AABB box) {
        RandomSource random = level.getRandom();
        BoxSurfaceSampler sampler = new BoxSurfaceSampler(box);
        for (int left = sampler.pointCount(); left > 0; left--) {
            Vec3 spot = sampler.sample(random);
            float red = SCAN_RED.roll(random);
            float green = SCAN_GREEN.roll(random);
            float blue = SCAN_BLUE.roll(random);
            float jitter = (float) random.nextGaussian() * SCAN_SCALE_DEVIATION;
            int delay = random.nextInt(SCAN_DELAY_RANGE);
            sparkle(level, random, spot.x, spot.y, spot.z, 0.0, 0.0, 0.0, red, green, blue, SCAN_SCALE_BASE + jitter, delay, SCAN_DECAY, SCAN_GRAVITY, SCAN_BASE_AGE);
        }
    }

    public static void sparkle(Level level, RandomSource random, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, float scale, int delay, float decay, float gravity, int baseAge) {
        submit(new SparkleParticleOptions(opaque(r, g, b), scale, delay, decay, gravity, baseAge, true), x, y, z, vx, vy, vz);
    }

    private static void submit(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        client.particleEngine.createParticle(options, x, y, z, vx, vy, vz);
    }

    private static int opaque(float r, float g, float b) {
        return ARGB.colorFromFloat(OPAQUE, r, g, b);
    }

    private static float channel(int color, int shift) {
        return ((color >> shift) & CHANNEL_MASK) / CHANNEL_MAX;
    }
}
