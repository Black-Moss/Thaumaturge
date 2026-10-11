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
    private static final HomingTuning HOMING = new HomingTuning(0.15, 2.0, 0.004, 0.006, 0.08);
    private static final int SAFETY_LIFETIME = 900;
    private static final int GROW_TICKS = 10;
    private static final float SIZE = 0.09F;
    private static final float PULSE_AMPLITUDE = 0.15F;
    private static final float PULSE_RATE = 0.4F;
    private static final float NEAR_SHRINK = 0.96F;
    private static final float DRAG = 0.95F;
    private static final double SPAWN_KICK = 0.015;
    private static final double TARGET_JITTER = 0.06;
    private static final float GREEN_RED = 0.35F;
    private static final float GREEN_GREEN = 0.9F;
    private static final float GREEN_BLUE = 0.3F;
    private static final float GREEN_VARIATION = 0.08F;

    private final HomingSteering homing = new HomingSteering(HOMING);
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float pulsePhase;
    private float proximityScale = 1.0F;

    private VisSparkleParticle(ClientLevel level, double x, double y, double z, VisSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        if (options.color() == VisSparkleParticleOptions.DEFAULT_COLOR) {
            setColor(vary(GREEN_RED), vary(GREEN_GREEN), vary(GREEN_BLUE));
        } else {
            setColor(options.color());
        }
        this.targetX = options.tx() + this.random.nextGaussian() * TARGET_JITTER;
        this.targetY = options.ty() + this.random.nextGaussian() * TARGET_JITTER;
        this.targetZ = options.tz() + this.random.nextGaussian() * TARGET_JITTER;
        this.friction = DRAG;
        this.lifetime = SAFETY_LIFETIME;
        this.alpha = 1.0F;
        this.quadSize = 0.0F;
        this.pulsePhase = this.random.nextFloat() * Mth.TWO_PI;
        jitterVelocity(SPAWN_KICK);
        frame(this.random.nextInt(sheetFrames()));
    }

    private float vary(float channel) {
        return Mth.clamp(channel + (this.random.nextFloat() * 2.0F - 1.0F) * GREEN_VARIATION, 0.0F, 1.0F);
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
        if (phase == HomingPhase.NEAR) {
            this.proximityScale *= NEAR_SHRINK;
        }
        float growth = Math.min(1.0F, (float) this.age / GROW_TICKS);
        float pulse = 1.0F + PULSE_AMPLITUDE * Mth.sin(this.age * PULSE_RATE + this.pulsePhase);
        this.quadSize = SIZE * growth * pulse * this.proximityScale;
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
