package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class TaintFumeParticle extends TTParticle {
    private static final int FRAME_COUNT = 3;
    private static final float SIZE_UNIT = 0.1F;
    private static final float END_SIZE_FACTOR = 0.25F;
    private static final float ALPHA_SCALE = 0.75F;
    private static final float SPIN_SPEED = 1.0F;
    private static final float RANDOM_RED_BASE = 0.4F;
    private static final float RANDOM_RED_RANGE = 0.2F;
    private static final float RANDOM_GREEN_BASE = 0.1F;
    private static final float RANDOM_GREEN_RANGE = 0.3F;
    private static final float RANDOM_BLUE_BASE = 0.5F;
    private static final float RANDOM_BLUE_RANGE = 0.2F;
    private static final int RANDOM_LIFETIME_BASE = 80;
    private static final int RANDOM_LIFETIME_RANGE = 20;
    private static final float RANDOM_FRICTION = 0.975F;
    private static final float RANDOM_GRAVITY = 0.2F;
    private static final int FIXED_LIFETIME_BASE = 10;
    private static final int FIXED_LIFETIME_RANGE = 10;
    private static final float FIXED_FRICTION = 0.9F;
    private static final float FIXED_GRAVITY = 0.0F;

    private final float startSize;
    private final float endSize;

    private TaintFumeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, TaintFumeParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.startSize = options.scale() * SIZE_UNIT;
        this.endSize = this.startSize * END_SIZE_FACTOR;
        this.quadSize = this.startSize;
        if (options.color() == TaintFumeParticleOptions.RANDOM_COLOR) {
            setColor(RANDOM_RED_BASE + this.random.nextFloat() * RANDOM_RED_RANGE, RANDOM_GREEN_BASE + this.random.nextFloat() * RANDOM_GREEN_RANGE,
                    RANDOM_BLUE_BASE + this.random.nextFloat() * RANDOM_BLUE_RANGE);
            this.lifetime = RANDOM_LIFETIME_BASE + this.random.nextInt(RANDOM_LIFETIME_RANGE);
            this.friction = RANDOM_FRICTION;
            this.gravity = RANDOM_GRAVITY;
        } else {
            setColor(options.color());
            this.lifetime = FIXED_LIFETIME_BASE + this.random.nextInt(FIXED_LIFETIME_RANGE);
            this.friction = FIXED_FRICTION;
            this.gravity = FIXED_GRAVITY;
        }
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? SPIN_SPEED : -SPIN_SPEED);
    }

    @Override
    protected void update() {
        float t = progress();
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        this.alpha = ALPHA_SCALE * (1.0F - t);
    }

    @Override
    public Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<TaintFumeParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("taint_fume");

        @Override
        public Particle createParticle(TaintFumeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new TaintFumeParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
