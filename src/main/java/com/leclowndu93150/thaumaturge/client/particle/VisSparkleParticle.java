package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class VisSparkleParticle extends TTParticle {
    private static final float DEFAULT_RED = 0.2F;
    private static final float DEFAULT_GREEN_BASE = 0.6F;
    private static final float DEFAULT_GREEN_SPAN = 0.3F;
    private static final float DEFAULT_BLUE = 0.2F;
    private static final float ALPHA = 0.5F;
    private static final int LIFETIME = 1000;
    private static final float INITIAL_NOISE = 0.01F;
    private static final float FRICTION = 0.985F;
    private static final int RAMP_DIVISOR_BASE = 45;
    private static final int RAMP_DIVISOR_RANGE = 15;
    private static final int RAMP_TICKS = 10;
    private static final double ARRIVAL_DISTANCE = 0.2;
    private static final double SHRINK_DISTANCE = 2.0;
    private static final float SHRINK = 0.95F;
    private static final double STEER = 0.1;
    private static final double MAX_SPEED = 0.1;
    private static final double MIN_DISTANCE = 1.0E-4;
    private static final int FRAME_COUNT = 16;
    private static final float SIZE_SCALE = 0.1F;
    private static final float SIZE_BASE = 6.0F;
    private static final float SIZE_PULSE = 0.3F;
    private static final float PULSE_PERIOD = 3.0F;

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int rampDivisor;
    private float size;

    private VisSparkleParticle(ClientLevel level, double x, double y, double z, VisSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.targetX = options.tx();
        this.targetY = options.ty();
        this.targetZ = options.tz();
        if (options.color() >= 0) {
            setColor(options.color());
        } else {
            setColor(DEFAULT_RED, DEFAULT_GREEN_BASE + this.random.nextFloat() * DEFAULT_GREEN_SPAN, DEFAULT_BLUE);
        }
        this.alpha = ALPHA;
        this.lifetime = LIFETIME;
        this.friction = FRICTION;
        this.rampDivisor = RAMP_DIVISOR_BASE + this.random.nextInt(RAMP_DIVISOR_RANGE);
        drift(INITIAL_NOISE, INITIAL_NOISE, INITIAL_NOISE);
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
        if (this.age < RAMP_TICKS) {
            this.size = (float) this.age / this.rampDivisor;
        }
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < ARRIVAL_DISTANCE) {
            remove();
            return;
        }
        if (distance < SHRINK_DISTANCE) {
            this.size *= SHRINK;
        }
        double inverse = STEER / Math.max(distance, MIN_DISTANCE);
        this.xd = Mth.clamp(this.xd + dx * inverse, -MAX_SPEED, MAX_SPEED);
        this.yd = Mth.clamp(this.yd + dy * inverse, -MAX_SPEED, MAX_SPEED);
        this.zd = Mth.clamp(this.zd + dz * inverse, -MAX_SPEED, MAX_SPEED);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return SIZE_SCALE * this.size * (SIZE_BASE + SIZE_PULSE * Mth.sin(this.age / PULSE_PERIOD));
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<VisSparkleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("vis_sparkle");

        @Override
        public Particle createParticle(VisSparkleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new VisSparkleParticle(level, x, y, z, options, SHEET);
        }
    }
}
