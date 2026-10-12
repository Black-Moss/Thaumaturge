package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.HomingPhase;
import com.leclowndu93150.thaumaturge.client.particle.support.HomingSteering;
import com.leclowndu93150.thaumaturge.client.particle.support.HomingTuning;
import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class VisSparkleParticle extends TTParticle {
    private static final HomingTuning HOMING = new HomingTuning(0.2, 2.0, 0.1, 0.1, 0.1);
    private static final int SAFETY_LIFETIME = 1000;
    private static final int GROW_TICKS = 9;
    private static final int GROWTH_DIVISOR_MIN = 45;
    private static final int GROWTH_DIVISOR_SPAN = 15;
    private static final float SIZE_BASE = 0.6F;
    private static final float PULSE_AMPLITUDE = 0.03F;
    private static final float PULSE_RATE = 1.0F / 3.0F;
    private static final float NEAR_SHRINK = 0.95F;
    private static final float DRAG = 0.985F;
    private static final float ALPHA = 0.5F;
    private static final double SPAWN_KICK = 0.01;
    private static final float GREEN_RED = 0.2F;
    private static final float GREEN_GREEN_BASE = 0.6F;
    private static final float GREEN_GREEN_RANGE = 0.3F;
    private static final float GREEN_BLUE = 0.2F;

    private final HomingSteering homing = new HomingSteering(HOMING);
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int growthDivisor;
    private float proximityScale = 1.0F;

    private VisSparkleParticle(ClientLevel level, double x, double y, double z, VisSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        if (options.color() == VisSparkleParticleOptions.DEFAULT_COLOR) {
            setColor(GREEN_RED, GREEN_GREEN_BASE + this.random.nextFloat() * GREEN_GREEN_RANGE, GREEN_BLUE);
        } else {
            setColor(options.color());
        }
        this.targetX = options.tx();
        this.targetY = options.ty();
        this.targetZ = options.tz();
        this.friction = DRAG;
        this.lifetime = SAFETY_LIFETIME;
        this.alpha = ALPHA;
        this.quadSize = 0.0F;
        this.growthDivisor = GROWTH_DIVISOR_MIN + this.random.nextInt(GROWTH_DIVISOR_SPAN);
        jitterVelocity(SPAWN_KICK);
        frame(this.random.nextInt(sheetFrames()));
    }

    @Override
    protected void update() {
        frame(this.age % sheetFrames());
        HomingPhase phase = this.homing.step(this.x, this.y, this.z, this.targetX, this.targetY, this.targetZ, this.xd, this.yd, this.zd);
        if (phase == HomingPhase.ARRIVED) {
            remove();
            return;
        }
        this.xd = this.homing.velocityX();
        this.yd = this.homing.velocityY();
        this.zd = this.homing.velocityZ();
        if (this.age <= GROW_TICKS) {
            this.proximityScale = phase == HomingPhase.NEAR ? NEAR_SHRINK : 1.0F;
        } else if (phase == HomingPhase.NEAR) {
            this.proximityScale *= NEAR_SHRINK;
        }
        float growth = (float) Math.min(this.age, GROW_TICKS) / this.growthDivisor;
        float pulse = SIZE_BASE + PULSE_AMPLITUDE * Mth.sin(this.age * PULSE_RATE);
        this.quadSize = growth * pulse * this.proximityScale;
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
