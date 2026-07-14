package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.content.fx.data.BoreParticlesData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class BoreParticlesParticle extends HomingParticleBase {
    private final TextureAtlasSprite sprite;
    private final SingleQuadParticle.Layer layer;
    private final float uo;
    private final float vo;

    private BoreParticlesParticle(ClientLevel level, double x, double y, double z, BoreParticlesData data, TextureAtlasSprite sprite) {
        super(level, x, y, z, data.targetEntityId(), data.tx(), data.ty(), data.tz(),
                data.sx(), data.sy(), data.sz(), 0.4F, 0.3F, 0.01F, 0.005F, sprite);
        this.sprite = sprite;
        this.layer = SingleQuadParticle.Layer.bySprite(this.sprite);
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize * 0.1F;
    }

    public static final class Provider implements ParticleProvider<BoreParticlesData> {
        @Override
        public @Nullable Particle createParticle(BoreParticlesData options, ClientLevel level, double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            if (options.state().isAir()) {
                return null;
            }
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                    .getParticleMaterial(options.state()).sprite();
            return new BoreParticlesParticle(level, x, y, z, options, sprite);
        }
    }
}
