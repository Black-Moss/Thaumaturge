package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.client.fx.render.texture.TCParticleLayer;
import com.leclowndu93150.thaumcraft.content.fx.data.FireMoteData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class FXFireMoteParticle extends SingleQuadParticle {
    private static final int GRID = 64;
    private static final int CELL = 7;
    private static final int FIXED_LIFETIME = 16;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    private final float baseScale;
    private final boolean translucent;
    private float baseAlpha = 1.0F;
    private float prevRoll;
    private float roll;

    private FXFireMoteParticle(ClientLevel level, double x, double y, double z, FireMoteData data, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.xd = data.vx();
        this.yd = data.vy();
        this.zd = data.vz();
        this.xo = x;
        this.yo = y;
        this.zo = z;
        float r = data.r() > 1.0F ? data.r() / 255.0F : data.r();
        float g = data.g() > 1.0F ? data.g() / 255.0F : data.g();
        float b = data.b() > 1.0F ? data.b() / 255.0F : data.b();
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = data.alpha();
        this.lifetime = FIXED_LIFETIME;
        this.baseScale = data.scale();
        this.quadSize = this.baseScale;
        this.translucent = data.translucent();
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.roll = (float)(Math.PI * 2.0);
        this.prevRoll = this.roll;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRoll = this.roll;
        super.tick();
        if (this.random.nextInt(6) == 0) {
            this.age++;
        }
        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }
        float lifespan = (float)this.age / this.lifetime;
        this.quadSize = this.baseScale - this.baseScale * lifespan;
        this.baseAlpha = 1.0F - lifespan;
        this.roll = this.roll + 1.0F;
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partialTickTime);
        float r = Mth.lerp(partialTickTime, this.prevRoll, this.roll);
        if (r != 0.0F) {
            rotation.rotateZ(r);
        }
        Vec3 pos = camera.position();
        float dx = (float)(Mth.lerp((double)partialTickTime, this.xo, this.x) - pos.x());
        float dy = (float)(Mth.lerp((double)partialTickTime, this.yo, this.y) - pos.y());
        float dz = (float)(Mth.lerp((double)partialTickTime, this.zo, this.z) - pos.z());
        int color = ARGB.colorFromFloat(this.alpha * this.baseAlpha, this.rCol, this.gCol, this.bCol);
        renderState.add(
                this.getLayer(),
                dx, dy, dz,
                rotation.x, rotation.y, rotation.z, rotation.w,
                this.getQuadSize(partialTickTime),
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                color, EMISSIVE_LIGHT);
    }

    @Override
    protected float getU0() {
        float cellW = 1.0F / GRID;
        return (CELL % GRID) * cellW;
    }

    @Override
    protected float getU1() {
        float cellW = 1.0F / GRID;
        return (CELL % GRID) * cellW + cellW;
    }

    @Override
    protected float getV0() {
        float cellH = 1.0F / GRID;
        return (CELL / GRID) * cellH;
    }

    @Override
    protected float getV1() {
        float cellH = 1.0F / GRID;
        return (CELL / GRID) * cellH + cellH;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.translucent ? TCParticleLayer.TRANSLUCENT : TCParticleLayer.ADDITIVE;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * 0.1F;
    }

    public static final class Provider implements ParticleProvider<FireMoteData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(FireMoteData options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new FXFireMoteParticle(level, x, y, z, options, this.sprites.first());
        }
    }
}
