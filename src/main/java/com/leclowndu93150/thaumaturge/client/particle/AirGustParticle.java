package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class AirGustParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float FRICTION = 0.75F;
    private static final float GRAVITY = -0.1F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float END_SIZE_FACTOR = 2.0F;
    private static final float SPIN_DIVISOR = 2.0F;
    private static final float ALPHA_SCALE = 0.5F;

    private final float startSize;
    private final float endSize;

    private AirGustParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, AirGustParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.friction = FRICTION;
        this.gravity = GRAVITY;
        this.startSize = options.scale() * SIZE_UNIT;
        this.endSize = this.startSize * END_SIZE_FACTOR;
        this.quadSize = this.startSize;
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian() / SPIN_DIVISOR);
        frame(0);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = ALPHA_SCALE * (1.0F - t);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        frameByProgress();
    }

    public static final class Provider implements ParticleProvider<AirGustParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("air_gust");

        @Override
        public Particle createParticle(AirGustParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new AirGustParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
