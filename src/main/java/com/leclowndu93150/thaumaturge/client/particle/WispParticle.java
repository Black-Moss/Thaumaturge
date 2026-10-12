package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.TargetEntityResolver;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class WispParticle extends TTParticle {
    private static final double LIFETIME_NUMERATOR = 40.0;
    private static final double LIFETIME_DIVISOR_MIN = 0.7;
    private static final double LIFETIME_DIVISOR_RANGE = 0.3;
    private static final float SIZE_BASE = 0.04F;
    private static final float SIZE_RANGE = 0.04F;
    private static final float HITBOX = 0.01F;
    private static final float SHADE_MAX = 0.05F;
    private static final float PEAK_ALPHA = 0.2F;
    private static final double FADE_DISTANCE = 32.0;
    private static final double SIDEWAYS_SPEED = 0.03;
    private static final double SINK_SPEED = -0.05;
    private static final float DRAG = 0.98F;

    private final @Nullable TargetEntityResolver ownerResolver;
    private float ageAlpha = PEAK_ALPHA;

    private WispParticle(ClientLevel level, double x, double y, double z, WispParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.ownerResolver = options.entityId() == WispParticleOptions.NO_ENTITY ? null : new TargetEntityResolver(options.entityId());
        setColor(shade(), shade(), shade());
        this.lifetime = (int) (LIFETIME_NUMERATOR / (LIFETIME_DIVISOR_MIN + this.random.nextDouble() * LIFETIME_DIVISOR_RANGE));
        this.quadSize = SIZE_BASE + this.random.nextFloat() * SIZE_RANGE;
        setSize(HITBOX, HITBOX);
        this.alpha = PEAK_ALPHA;
        this.friction = DRAG;
        this.xd = this.random.nextGaussian() * SIDEWAYS_SPEED;
        this.yd = SINK_SPEED;
        this.zd = this.random.nextGaussian() * SIDEWAYS_SPEED;
    }

    private float shade() {
        return this.random.nextFloat() * SHADE_MAX;
    }

    @Override
    protected void update() {
        frame(this.age % sheetFrames());
        this.ageAlpha = PEAK_ALPHA * (1.0F - progress());
        followOwner();
    }

    private void followOwner() {
        if (this.ownerResolver == null) {
            return;
        }
        Entity owner = this.ownerResolver.resolve(this.level);
        if (owner == null || owner.isRemoved()) {
            return;
        }
        this.x += owner.getX() - owner.xo;
        this.z += owner.getZ() - owner.zo;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        double distance = Math.sqrt(camera.position().distanceToSqr(this.x, this.y, this.z));
        double ratio = Math.min(distance / FADE_DISTANCE, 1.0);
        this.alpha = Mth.clamp(this.ageAlpha * (float) (1.0 - ratio * ratio), 0.0F, 1.0F);
        if (this.alpha > 0.0F) {
            super.extract(state, camera, partialTick);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<WispParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("wisp");

        @Override
        public Particle createParticle(WispParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new WispParticle(level, x, y, z, options, SHEET);
        }
    }
}
