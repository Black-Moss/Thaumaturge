package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.EarthPebbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class EarthPebbleParticle extends TTParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float FRICTION = 0.9F;
    private static final float GRAVITY = 0.4F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float END_SIZE_FACTOR = 0.5F;

    private final ParticleSheet pebbleSheet;
    private final float startSize;
    private final float endSize;

    private EarthPebbleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, EarthPebbleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.pebbleSheet = sheet;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.friction = FRICTION;
        this.gravity = GRAVITY;
        this.startSize = options.scale() * SIZE_UNIT;
        this.endSize = this.startSize * END_SIZE_FACTOR;
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian());
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = 1.0F - t;
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
    }

    @Override
    public Layer getLayer() {
        return TTParticleLayers.translucent(this.pebbleSheet);
    }

    public static final class Provider implements ParticleProvider<EarthPebbleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("earth_pebble");

        @Override
        public Particle createParticle(EarthPebbleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new EarthPebbleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
