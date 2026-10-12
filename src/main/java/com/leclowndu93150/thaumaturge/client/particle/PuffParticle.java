package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class PuffParticle extends TTParticle {
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 15;
    private static final float START_SIZE = 0.3F;
    private static final float END_SIZE_BASE = 0.4F;
    private static final float END_SIZE_RANGE = 0.3F;
    private static final float START_ALPHA = 1.0F;
    private static final float END_ALPHA = 0.1F;
    private static final float DRAG = 0.7F;
    private static final float SPIN_SPEED = 1.0F;

    private final float endSize;

    private PuffParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.alpha = START_ALPHA;
        this.friction = DRAG;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = START_SIZE;
        this.endSize = END_SIZE_BASE + this.random.nextFloat() * END_SIZE_RANGE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? SPIN_SPEED : -SPIN_SPEED);
    }

    @Override
    protected void update() {
        float t = progress();
        frameByProgress();
        this.quadSize = Mth.lerp(t, START_SIZE, this.endSize);
        this.alpha = Mth.lerp(t, START_ALPHA, END_ALPHA);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("puff");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PuffParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
