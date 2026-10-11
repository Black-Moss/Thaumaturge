package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class NitorCoreParticle extends SingleQuadParticle {
    private static final float QUAD_RADIUS = 0.1F;
    private static final int LIFETIME = 10;
    private static final float PULSE_DEVIATION = 0.12F;
    private static final double POSITION_JITTER = 0.004;

    private final ParticleSheet sheet;
    private final float midBoost;

    private NitorCoreParticle(ClientLevel level, double x, double y, double z, NitorCoreParticleOptions data, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, (TextureAtlasSprite) null);
        this.sheet = sheet;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        setPos(x + this.random.nextGaussian() * POSITION_JITTER, y + this.random.nextGaussian() * POSITION_JITTER, z + this.random.nextGaussian() * POSITION_JITTER);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.rCol = data.r();
        this.gCol = data.g();
        this.bCol = data.b();
        this.alpha = 1.0F;
        this.lifetime = LIFETIME;
        this.midBoost = 1.0F + (float) this.random.nextGaussian() * PULSE_DEVIATION;
        this.quadSize = 1.0F;
        this.hasPhysics = false;
        this.gravity = 0.0F;
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
        float t = Mth.clamp((float) this.age / this.lifetime, 0.0F, 1.0F);
        this.quadSize = 1.0F + (this.midBoost - 1.0F) * Mth.sin(t * Mth.PI);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TTParticleLayers.additive(this.sheet);
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

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * QUAD_RADIUS;
    }

    public static final class Provider implements ParticleProvider<NitorCoreParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("nitor_core");

        @Override
        public Particle createParticle(NitorCoreParticleOptions options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new NitorCoreParticle(level, x, y, z, options, SHEET);
        }
    }
}
