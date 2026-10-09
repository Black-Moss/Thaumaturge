package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.HomingPhase;
import com.leclowndu93150.thaumaturge.client.particle.support.HomingSteering;
import com.leclowndu93150.thaumaturge.client.particle.support.HomingTuning;
import com.leclowndu93150.thaumaturge.client.particle.support.TargetEntityResolver;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class WispyMoteParticle extends TTParticle {
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final double WIND_SCALE = 0.001;
    private static final float DRIFT = 0.0025F;
    private static final int FRAME_COUNT = 16;
    private static final float PEAK_ALPHA = 0.6F;
    private static final float SIZE_START = 0.1F;
    private static final float SIZE_END = 0.05F;
    private static final float LIFETIME_JITTER = 0.5F;
    private static final float NEAR_TARGET_SHRINK = 0.9F;
    private static final double ARRIVAL_DISTANCE = 0.25;
    private static final double NEAR_DISTANCE = 4.0;
    private static final double PUSH = 0.3;
    private static final double PUSH_NEAR = 0.6;
    private static final double MAX_SPEED = 0.35;
    private static final HomingTuning HOMING = new HomingTuning(ARRIVAL_DISTANCE, NEAR_DISTANCE, PUSH, PUSH_NEAR, MAX_SPEED);

    private final @Nullable TargetEntityResolver targetResolver;
    private final HomingSteering homing = new HomingSteering(HOMING);
    private final boolean emissive;
    private float proximityScale = 1.0F;

    private WispyMoteParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, WispyMoteParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = 0.0F;
        this.quadSize = SIZE_START;
        this.lifetime = (int) (options.age() + options.age() * LIFETIME_JITTER * this.random.nextFloat());
        this.gravity = options.gravity();
        this.targetResolver = options.targetEntityId() == WispyMoteParticleOptions.NO_ENTITY ? null : new TargetEntityResolver(options.targetEntityId());
        this.emissive = options.emissive();
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        drift(DRIFT, 0.0F, DRIFT);
        frame(this.age % FRAME_COUNT);
        if (this.targetResolver != null) {
            homeOnTarget(this.targetResolver);
        }
        applyLifetimeCurve(progress());
    }

    private void applyLifetimeCurve(float progress) {
        this.alpha = PEAK_ALPHA * Keyframes.sample(progress, 0.0F, 1.0F, 1.0F, 0.0F);
        this.quadSize = Mth.lerp(progress, SIZE_START, SIZE_END) * this.proximityScale;
    }

    private void homeOnTarget(TargetEntityResolver resolver) {
        Entity target = resolver.resolve(this.level);
        if (target == null) {
            return;
        }
        if (!target.isAlive()) {
            remove();
            return;
        }
        Vec3 position = target.position();
        HomingPhase phase = this.homing.step(this.x, this.y, this.z, position.x, position.y, position.z, this.xd, this.yd, this.zd);
        if (phase == HomingPhase.ARRIVED) {
            remove();
            return;
        }
        this.xd = this.homing.velocityX();
        this.yd = this.homing.velocityY();
        this.zd = this.homing.velocityZ();
        if (phase == HomingPhase.NEAR) {
            this.proximityScale *= NEAR_TARGET_SHRINK;
        }
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return this.emissive ? EMISSIVE_LIGHT : super.getLightCoords(partialTick);
    }

    public static final class Provider implements ParticleProvider<WispyMoteParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("wispy_mote");

        @Override
        public Particle createParticle(WispyMoteParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new WispyMoteParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
