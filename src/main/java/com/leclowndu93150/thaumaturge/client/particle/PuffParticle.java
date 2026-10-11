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
    private static final int LIFETIME_RANGE = 18;
    private static final float START_SIZE = 0.12F;
    private static final float END_SIZE_BASE = 0.24F;
    private static final float END_SIZE_RANGE = 0.12F;
    private static final float START_ALPHA = 1.0F;
    private static final float END_ALPHA = 0.12F;
    private static final float DRAG = 0.72F;
    private static final float SPIN_BASE = 0.004F;
    private static final float SPIN_RANGE = 0.004F;
    private static final float SHADE_JITTER = 0.03F;

    private final float endSize;

    private PuffParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(shade(options.getRed()), shade(options.getGreen()), shade(options.getBlue()));
        this.alpha = START_ALPHA;
        this.friction = DRAG;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = START_SIZE;
        this.endSize = END_SIZE_BASE + this.random.nextFloat() * END_SIZE_RANGE;
        float spin = SPIN_BASE + this.random.nextFloat() * SPIN_RANGE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? spin : -spin);
    }

    private float shade(float channel) {
        return Mth.clamp(channel + (this.random.nextFloat() * 2.0F - 1.0F) * SHADE_JITTER, 0.0F, 1.0F);
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
