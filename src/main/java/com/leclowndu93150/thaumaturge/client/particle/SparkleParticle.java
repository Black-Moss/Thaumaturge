package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SparkleParticle extends TTParticle {
    private static final int LOOP_FRAMES = 16;
    private static final int SMALL_LOOP_START = 0;
    private static final int NORMAL_LOOP_START = 16;
    private static final int SMALL_ODDS = 5;
    private static final float SIZE_UNIT = 0.25F;
    private static final float GROWTH = 2.0F;
    private static final int LIFE_FACTOR = 3;
    private static final int LIFE_EXTRA_RANGE = 5;
    private static final int TICKS_PER_FLICKER = 6;
    private static final int MIN_FLICKER_LEVELS = 2;
    private static final float FLICKER_FLOOR = 0.25F;
    private static final float HORIZONTAL_NUDGE = 0.002F;
    private static final float VERTICAL_NUDGE = 0.003F;
    private static final float STEADY_NUDGE = 0.001F;
    private static final double WIND_SCALE = 0.003;

    private final boolean flicker;
    private final int loopStart;
    private final int loopOffset;
    private final float baseSize;
    private final float[] flickerKeys;

    private SparkleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.flicker = options.flicker();
        this.friction = options.decay();
        this.gravity = options.gravity();
        this.lifetime = Math.max(1, options.baseAge()) * LIFE_FACTOR + this.random.nextInt(LIFE_EXTRA_RANGE);
        this.baseSize = options.scale() * SIZE_UNIT;
        this.quadSize = this.baseSize;
        this.alpha = 0.0F;
        this.loopStart = this.random.nextInt(SMALL_ODDS) == 0 ? SMALL_LOOP_START : NORMAL_LOOP_START;
        this.loopOffset = this.random.nextInt(LOOP_FRAMES);
        this.flickerKeys = this.flicker ? buildFlickerKeys() : new float[0];
        frame(this.loopStart + this.loopOffset);
        setDelay(options.delay());
        if (this.flicker) {
            setMoonWind(WIND_SCALE);
        }
    }

    private float[] buildFlickerKeys() {
        int levels = MIN_FLICKER_LEVELS + this.lifetime / TICKS_PER_FLICKER;
        float[] keys = new float[levels + 2];
        for (int i = 1; i <= levels; i++) {
            keys[i] = FLICKER_FLOOR + this.random.nextFloat() * (1.0F - FLICKER_FLOOR);
        }
        return keys;
    }

    @Override
    protected void update() {
        frame(this.loopStart + (this.age + this.loopOffset) % LOOP_FRAMES);
        float t = progress();
        if (this.flicker) {
            drift(HORIZONTAL_NUDGE, VERTICAL_NUDGE, HORIZONTAL_NUDGE);
            this.alpha = Keyframes.sample(t, this.flickerKeys);
            this.quadSize = Mth.lerp(t, this.baseSize, this.baseSize * GROWTH);
        } else {
            drift(STEADY_NUDGE, 0.0F, STEADY_NUDGE);
            this.alpha = Keyframes.sample(t, 0.0F, 1.0F, 0.0F);
            this.quadSize = this.baseSize * Keyframes.sample(t, 1.0F, GROWTH, 1.0F);
        }
    }

    public static final class Provider implements ParticleProvider<SparkleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("sparkle");

        @Override
        public Particle createParticle(SparkleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SparkleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
