package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class FrostFlakeParticle extends TTParticle {
    private static final int LIFETIME_BASE = 40;
    private static final int LIFETIME_RANGE = 40;
    private static final float FRICTION = 0.8F;
    private static final float GRAVITY = 0.033F;
    private static final float SIZE_UNIT = 0.1F;
    private static final float JITTER_HORIZONTAL = 0.0025F;
    private static final float JITTER_VERTICAL = 0.0001F;
    private static final float START_TURNS_RANGE = 3.0F;
    private static final float SPIN_DEVIATION = 0.25F;

    private FrostFlakeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FrostFlakeParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.friction = FRICTION;
        this.gravity = GRAVITY;
        this.quadSize = options.scale() * SIZE_UNIT;
        setSpin(this.random.nextFloat() * START_TURNS_RANGE, (float) this.random.nextGaussian() * SPIN_DEVIATION);
    }

    @Override
    protected void update() {
        drift(JITTER_HORIZONTAL, JITTER_VERTICAL, JITTER_HORIZONTAL);
        this.alpha = 1.0F - progress();
    }

    public static final class Provider implements ParticleProvider<FrostFlakeParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("frost_flake");

        @Override
        public Particle createParticle(FrostFlakeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FrostFlakeParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
