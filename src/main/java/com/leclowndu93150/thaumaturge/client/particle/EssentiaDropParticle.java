package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.EssentiaDropParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

public final class EssentiaDropParticle extends SingleQuadParticle {
    private static final double VELOCITY_NOISE = 0.005;
    private static final int LIFETIME_BASE = 20;
    private static final int LIFETIME_RANGE = 10;
    private static final float SIZE_BASE = 0.4F;
    private static final float SIZE_RANGE = 0.2F;
    private static final float GRAVITY = 0.01F;
    private static final float FRICTION = 0.98F;
    private static final float GRAVITY_FACTOR = 0.04F;
    private static final float SIZE_FADE_SHARE = 0.5F;

    private final ParticleSheet sheet;
    private final float baseAlpha;
    private final float baseSize;

    private EssentiaDropParticle(ClientLevel level, double x, double y, double z, EssentiaDropParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, null);
        this.sheet = sheet;
        RandomSource levelRandom = level.getRandom();
        this.xd = levelRandom.nextGaussian() * VELOCITY_NOISE;
        this.yd = levelRandom.nextGaussian() * VELOCITY_NOISE;
        this.zd = levelRandom.nextGaussian() * VELOCITY_NOISE;
        this.rCol = ARGB.red(options.color()) / 255.0F;
        this.gCol = ARGB.green(options.color()) / 255.0F;
        this.bCol = ARGB.blue(options.color()) / 255.0F;
        this.baseAlpha = options.alpha();
        this.alpha = this.baseAlpha;
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.baseSize = SIZE_BASE + this.random.nextFloat() * SIZE_RANGE;
        this.quadSize = this.baseSize;
        this.gravity = GRAVITY;
        this.friction = FRICTION;
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }
        this.yd -= GRAVITY_FACTOR * this.gravity;
        move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        float fade = 1.0F - (float) this.age / this.lifetime;
        this.alpha = this.baseAlpha * fade;
        this.quadSize = this.baseSize * (1.0F - SIZE_FADE_SHARE + SIZE_FADE_SHARE * fade);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    @Override
    protected float getU0() {
        return 0.0F;
    }

    @Override
    protected float getU1() {
        return 1.0F;
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    public static final class Provider implements ParticleProvider<EssentiaDropParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("essentia_drop");

        @Override
        public Particle createParticle(EssentiaDropParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new EssentiaDropParticle(level, x, y, z, options, SHEET);
        }
    }
}
