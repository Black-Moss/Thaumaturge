package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class PollutionFumeParticle extends TTParticle {
    private static final int LIFETIME_BASE = 100;
    private static final int LIFETIME_RANGE = 60;
    private static final float RED = 1.0F;
    private static final float GREEN = 0.3F;
    private static final float BLUE = 0.9F;
    private static final double HORIZONTAL_SPREAD = 0.005;
    private static final double RISE_SPEED = 0.02;
    private static final float START_SIZE = 0.2F;
    private static final float END_SIZE = 0.5F;
    private static final float ALPHA_SCALE = 0.5F;
    private static final float SPIN_SPEED = 1.0F;
    private static final double WIND_SCALE = 0.001;

    private PollutionFumeParticle(ClientLevel level, double x, double y, double z, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(RED, GREEN, BLUE);
        this.xd = (this.random.nextDouble() - this.random.nextDouble()) * HORIZONTAL_SPREAD;
        this.yd = RISE_SPEED;
        this.zd = (this.random.nextDouble() - this.random.nextDouble()) * HORIZONTAL_SPREAD;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = START_SIZE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? SPIN_SPEED : -SPIN_SPEED);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = ALPHA_SCALE * (1.0F - t);
        this.quadSize = Keyframes.sample(t, START_SIZE, END_SIZE);
    }

    @Override
    public Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("pollution_fume");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PollutionFumeParticle(level, x, y, z, SHEET);
        }
    }
}
