package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.support.TargetEntityResolver;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class WispParticle extends TTParticle {
    private static final int LIFETIME_BASE = 40;
    private static final int LIFETIME_RANGE = 20;
    private static final float SIZE = 0.22F;
    private static final float SHADE_BASE = 0.03F;
    private static final float SHADE_TINT = 0.09F;
    private static final float PEAK_ALPHA = 0.3F;
    private static final double FADE_DISTANCE = 32.0;
    private static final double SIDEWAYS_SPEED = 0.015;
    private static final double SINK_SPEED = -0.008;
    private static final float DRAG = 0.95F;
    private static final int TICKS_PER_FRAME = 3;

    private final @Nullable TargetEntityResolver ownerResolver;
    private final int frameOffset;
    private float ageAlpha = PEAK_ALPHA;

    private WispParticle(ClientLevel level, double x, double y, double z, WispParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.ownerResolver = options.entityId() == WispParticleOptions.NO_ENTITY ? null : new TargetEntityResolver(options.entityId());
        setColor(shade(), shade(), shade());
        this.lifetime = LIFETIME_BASE + this.random.nextInt(LIFETIME_RANGE);
        this.quadSize = SIZE;
        this.alpha = PEAK_ALPHA;
        this.friction = DRAG;
        this.hasPhysics = true;
        this.xd = this.random.nextGaussian() * SIDEWAYS_SPEED;
        this.yd = SINK_SPEED;
        this.zd = this.random.nextGaussian() * SIDEWAYS_SPEED;
        this.frameOffset = this.random.nextInt(TICKS_PER_FRAME * sheetFrames());
        setSpin(this.random.nextFloat(), 0.0F);
    }

    private float shade() {
        return SHADE_BASE + this.random.nextFloat() * SHADE_TINT;
    }

    @Override
    protected void update() {
        frame((this.age + this.frameOffset) / TICKS_PER_FRAME % sheetFrames());
        this.ageAlpha = PEAK_ALPHA * (1.0F - progress());
        followOwner();
    }

    private void followOwner() {
        if (this.ownerResolver == null || this.onGround) {
            return;
        }
        Entity owner = this.ownerResolver.resolve(this.level);
        if (owner == null || owner.isRemoved()) {
            return;
        }
        move(owner.getX() - owner.xo, 0.0, owner.getZ() - owner.zo);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        double distance = Math.sqrt(camera.position().distanceToSqr(this.x, this.y, this.z));
        this.alpha = this.ageAlpha * (float) Mth.clamp(1.0 - distance / FADE_DISTANCE, 0.0, 1.0);
        if (this.alpha > 0.0F) {
            super.extract(state, camera, partialTick);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.translucent(this.sheet);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<WispParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("wisp");

        @Override
        public Particle createParticle(WispParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new WispParticle(level, x, y, z, options, SHEET);
        }
    }
}
