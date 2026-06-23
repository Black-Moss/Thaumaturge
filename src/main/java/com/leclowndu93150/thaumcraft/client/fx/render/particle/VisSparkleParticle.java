package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.content.fx.data.VisSparkleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class VisSparkleParticle extends SingleQuadParticle {
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float sizeMod;
    private final SpriteSet sprites;

    private static final int FRAME_COUNT = 16;
    private static final int FIXED_LIFETIME = 1000;

    private VisSparkleParticle(ClientLevel level, double x, double y, double z, VisSparkleData data, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprites.first());
        this.sprites = sprites;
        this.cycleSprite();
        this.rCol = 0.2F;
        this.gCol = 0.6F + level.getRandom().nextFloat() * 0.3F;
        this.bCol = 0.2F;
        this.alpha = 0.5F;
        this.quadSize = 0.0F;
        this.targetX = data.tx();
        this.targetY = data.ty();
        this.targetZ = data.tz();
        this.lifetime = FIXED_LIFETIME;
        float f3 = 0.01F;
        this.xd = (float)level.getRandom().nextGaussian() * f3;
        this.yd = (float)level.getRandom().nextGaussian() * f3;
        this.zd = (float)level.getRandom().nextGaussian() * f3;
        this.sizeMod = 45 + level.getRandom().nextInt(15);
        this.gravity = 0.2F;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double d13 = 0.1;
        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d < 2.0) {
            this.quadSize *= 0.95F;
        }
        if (d < 0.2) {
            this.remove();
            return;
        }
        if (this.age < 10) {
            this.quadSize = this.age / this.sizeMod;
        }
        if (d > 1.0E-4) {
            dx /= d;
            dy /= d;
            dz /= d;
            this.xd += dx * d13;
            this.yd += dy * d13;
            this.zd += dz * d13;
        }
        this.xd = Mth.clamp((float)this.xd, -0.1F, 0.1F);
        this.yd = Mth.clamp((float)this.yd, -0.1F, 0.1F);
        this.zd = Mth.clamp((float)this.zd, -0.1F, 0.1F);
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.cycleSprite();
    }

    private void cycleSprite() {
        if (!this.removed) {
            this.sprite = this.sprites.get(this.age % FRAME_COUNT, FRAME_COUNT);
        }
    }

    @Override
    public float getQuadSize(float a) {
        float bob = Mth.sin(this.age / 3.0F) * 0.3F + 6.0F;
        return 0.1F * this.quadSize * bob;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Provider implements ParticleProvider<VisSparkleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(VisSparkleData options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new VisSparkleParticle(level, x, y, z, options, sprites);
        }
    }
}
