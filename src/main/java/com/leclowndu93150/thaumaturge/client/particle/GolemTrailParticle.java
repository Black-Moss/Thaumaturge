package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class GolemTrailParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 5;
    private static final float START_SIZE = 0.15F;
    private static final float MID_SIZE = 0.3F;
    private static final float END_SIZE = 0.8F;
    private static final float ALPHA_SCALE = 0.3F;
    private static final float SPIN_SPEED = 1.0F;
    private static final double WIND_SCALE = 0.001;

    private GolemTrailParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = START_SIZE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? SPIN_SPEED : -SPIN_SPEED);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = ALPHA_SCALE * (1.0F - t);
        this.quadSize = Keyframes.sample(t, START_SIZE, MID_SIZE, END_SIZE);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("golem_trail");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new GolemTrailParticle(level, x, y, z, vx, vy, vz, SHEET);
        }
    }
}
