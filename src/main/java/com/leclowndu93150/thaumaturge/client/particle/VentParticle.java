package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.ParticleCulling;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class VentParticle extends TTParticle {
    private static final float MIN_SCALE = 1.0E-4F;
    private static final float START_FRACTION_BASE = 0.05F;
    private static final float START_FRACTION_SPAN = 0.1F;
    private static final float GROWTH_RATE = 1.15F;
    private static final float GROWTH_RATE_VARIANT = 1.2F;
    private static final float HALF_EXTENT_PER_GROWTH = 0.3F;
    private static final float ALPHA_PEAK = 0.4F;
    private static final float FRAME_BASE = 1.0F;
    private static final float FRAME_SPAN = 4.0F;
    private static final float COLOR_NOISE = 0.05F;
    private static final double MIN_VELOCITY_SQR = 1.0E-12;
    private static final double VELOCITY_NOISE = 0.0375;
    private static final double LAUNCH_SPEED = 0.125;
    private static final double RISE = 0.0025;
    private static final double RISE_VARIANT_SPREAD = 0.0075;
    private static final float HITBOX = 0.02F;
    private static final float FRICTION = 0.85F;

    private final float fullScale;
    private final float startFraction;
    private final float growthRate;
    private final double rise;

    private VentParticle(ClientLevel level, double x, double y, double z, VentParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, options.vx(), options.vy(), options.vz(), sheet);
        this.fullScale = Math.max(options.scale(), MIN_SCALE);
        this.growthRate = options.variant() ? GROWTH_RATE_VARIANT : GROWTH_RATE;
        this.startFraction = this.random.nextFloat() * START_FRACTION_SPAN + START_FRACTION_BASE;
        this.lifetime = Integer.MAX_VALUE;
        this.hasPhysics = true;
        this.friction = FRICTION;
        setSize(HITBOX, HITBOX);
        setColor(options.color());
        if (options.variant()) {
            this.rCol = noisy(this.rCol);
            this.gCol = noisy(this.gCol);
            this.bCol = noisy(this.bCol);
            this.rise = this.random.nextGaussian() * RISE_VARIANT_SPREAD;
        } else {
            this.rise = RISE;
            launch();
        }
        applyFraction(this.startFraction);
    }

    private float noisy(float channel) {
        return Mth.clamp(channel + (float) (this.random.nextGaussian() * COLOR_NOISE), 0.0F, 1.0F);
    }

    private void launch() {
        double lengthSqr = this.xd * this.xd + this.yd * this.yd + this.zd * this.zd;
        if (lengthSqr < MIN_VELOCITY_SQR) {
            return;
        }
        double inverseLength = 1.0 / Math.sqrt(lengthSqr);
        this.xd *= inverseLength;
        this.yd *= inverseLength;
        this.zd *= inverseLength;
        jitterVelocity(VELOCITY_NOISE);
        this.xd *= LAUNCH_SPEED;
        this.yd *= LAUNCH_SPEED;
        this.zd *= LAUNCH_SPEED;
    }

    private float growthFraction() {
        return this.startFraction * (float) Math.pow(this.growthRate, this.age);
    }

    private void applyFraction(float fraction) {
        this.quadSize = HALF_EXTENT_PER_GROWTH * this.fullScale * fraction;
        this.alpha = ALPHA_PEAK * (1.0F - fraction);
        frame((int) (FRAME_BASE + FRAME_SPAN * fraction));
    }

    @Override
    public void tick() {
        this.yd += this.rise;
        super.tick();
    }

    @Override
    protected void update() {
        float fraction = growthFraction();
        if (fraction >= 1.0F) {
            remove();
            return;
        }
        applyFraction(fraction);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<VentParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("vent");

        @Override
        public @Nullable Particle createParticle(VentParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            if (ParticleCulling.beyondSpawnRange(x, y, z)) {
                return null;
            }
            return new VentParticle(level, x, y, z, options, SHEET);
        }
    }
}
