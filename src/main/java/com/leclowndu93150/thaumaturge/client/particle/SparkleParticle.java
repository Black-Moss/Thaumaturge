package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SparkleParticle extends TTParticle {
    private static final int LOOP_FRAMES = 16;
    private static final int LOW_LOOP_START = 0;
    private static final int HIGH_LOOP_START = 16;
    private static final int HIGH_LOOP_ODDS = 5;
    private static final float SIZE_UNIT = 0.1F;
    private static final float GROWTH = 2.0F;
    private static final int LIFE_FACTOR = 4;
    private static final int TICKS_PER_FLICKER = 4;
    private static final int FLICKER_KEY_PADDING = 2;
    private static final float FLICKER_CUTOFF_PROGRESS = 0.85F;
    private static final float HORIZONTAL_NUDGE = 0.0005F;
    private static final float VERTICAL_NUDGE = 0.001F;
    private static final double WIND_SCALE = 0.0005;

    private final boolean flicker;
    private final int loopStart;
    private final float baseSize;
    private final float[] flickerKeys;

    private SparkleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.flicker = options.flicker();
        this.friction = options.decay();
        this.gravity = options.gravity();
        int baseAge = Math.max(1, options.baseAge());
        this.lifetime = baseAge * LIFE_FACTOR + this.random.nextInt(baseAge);
        this.baseSize = options.scale() * SIZE_UNIT;
        this.quadSize = this.baseSize;
        this.alpha = 0.0F;
        this.loopStart = this.random.nextInt(HIGH_LOOP_ODDS) == 0 ? HIGH_LOOP_START : LOW_LOOP_START;
        this.flickerKeys = this.flicker ? buildFlickerKeys() : new float[0];
        frame(this.loopStart);
        setDelay(options.delay());
        setMoonWind(WIND_SCALE);
    }

    private float[] buildFlickerKeys() {
        float[] keys = new float[this.lifetime / TICKS_PER_FLICKER + FLICKER_KEY_PADDING];
        keys[0] = 1.0F;
        for (int i = 1; i < keys.length; i++) {
            float previousProgress = (float) ((i - 1) * TICKS_PER_FLICKER) / this.lifetime;
            keys[i] = i >= 2 && previousProgress > FLICKER_CUTOFF_PROGRESS ? 0.0F : this.random.nextFloat();
        }
        return keys;
    }

    private float flickerAlpha() {
        int segment = Math.min(this.age / TICKS_PER_FLICKER, this.flickerKeys.length - 2);
        float within = (float) (this.age - segment * TICKS_PER_FLICKER) / TICKS_PER_FLICKER;
        return Mth.lerp(within, this.flickerKeys[segment], this.flickerKeys[segment + 1]);
    }

    @Override
    protected void update() {
        frame(this.loopStart + this.age % LOOP_FRAMES);
        float t = progress();
        drift(HORIZONTAL_NUDGE, VERTICAL_NUDGE, HORIZONTAL_NUDGE);
        if (this.flicker) {
            this.alpha = flickerAlpha();
            this.quadSize = Mth.lerp(t, this.baseSize, this.baseSize * GROWTH);
        } else {
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
