package com.leclowndu93150.thaumcraft.client.renderer.particle;

import com.leclowndu93150.thaumcraft.content.particle.BoreSparkleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class BoreSparkleParticle extends HomingParticleBase {
    private final TextureAtlasSprite sprite;

    private BoreSparkleParticle(ClientLevel level, double x, double y, double z, BoreSparkleData data, TextureAtlasSprite sprite) {
        super(level, x, y, z, data.tx(), data.ty(), data.tz(), 0.01F, sprite);
        this.sprite = sprite;
        this.rCol = data.r();
        this.gCol = data.g();
        this.bCol = data.b();
        this.gravity = 0.2F;
        this.quadSize = this.random.nextFloat() * 0.5F + 0.5F;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float bob = Mth.sin(this.age / 3.0F) * 0.5F + 1.0F;
        return this.quadSize * 0.1F * bob;
    }

    @Override
    protected float getU0() {
        int frame = this.age % 4;
        return this.sprite.getU((frame + 1) / 4.0F);
    }

    @Override
    protected float getU1() {
        int frame = this.age % 4;
        return this.sprite.getU(frame / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(0.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV(1.0F);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.bySprite(this.sprite);
    }

    public static final class Provider implements ParticleProvider<BoreSparkleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(BoreSparkleData options, ClientLevel level, double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            return new BoreSparkleParticle(level, x, y, z, options, this.sprites.first());
        }
    }
}
