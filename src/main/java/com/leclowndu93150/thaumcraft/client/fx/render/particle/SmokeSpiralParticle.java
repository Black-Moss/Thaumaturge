package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.client.fx.render.texture.TCParticleLayer;
import com.leclowndu93150.thaumcraft.content.fx.data.SmokeSpiralData;
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

public final class SmokeSpiralParticle extends SingleQuadParticle {
    private static final int GRID = 16;
    private static final float QUAD_RADIUS = 0.15F;
    private static final float ALPHA = 0.66F;

    private final float radius;
    private final int startDeg;
    private final int minY;
    private int currentCell;
    private float currentAlpha = 1.0F;

    private SmokeSpiralParticle(ClientLevel level, double x, double y, double z, SmokeSpiralData data, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.setSize(0.01F, 0.01F);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.hasPhysics = false;
        this.gravity = -0.01F;
        this.lifetime = 20 + this.random.nextInt(10);
        this.radius = data.radius();
        this.startDeg = data.start();
        this.minY = data.minY();
        this.rCol = data.r();
        this.gCol = data.g();
        this.bCol = data.b();
        this.alpha = 1.0F;
        this.currentCell = 1;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.currentAlpha = (float)(this.lifetime - this.age) / this.lifetime;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.currentCell = (int)(1.0F + (float)this.age / this.lifetime * 4.0F);
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        float ageFrac = (this.age + partialTickTime) / this.lifetime;
        float r1 = this.startDeg + 720.0F * ageFrac;
        float r2 = 90.0F - 180.0F * ageFrac;
        float r1r = (float)Math.toRadians(r1);
        float r2r = (float)Math.toRadians(r2);
        float mX = -Mth.sin(r1r) * Mth.cos(r2r) * this.radius;
        float mY = -Mth.sin(r2r) * this.radius;
        float mZ = Mth.cos(r1r) * Mth.cos(r2r) * this.radius;
        Vec3 pos = camera.position();
        float baseX = (float)(Mth.lerp((double)partialTickTime, this.xo, this.x) - pos.x());
        float baseY = (float)(Math.max(Mth.lerp((double)partialTickTime, this.yo, this.y) + mY, this.minY + 0.1) - pos.y());
        float baseZ = (float)(Mth.lerp((double)partialTickTime, this.zo, this.z) - pos.z());
        float wx = baseX + mX;
        float wy = baseY;
        float wz = baseZ + mZ;
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partialTickTime);
        int color = ARGB.colorFromFloat(ALPHA * this.currentAlpha, this.rCol, this.gCol, this.bCol);
        int light = this.getLightCoords(partialTickTime);
        renderState.add(
                this.getLayer(),
                wx, wy, wz,
                rotation.x, rotation.y, rotation.z, rotation.w,
                QUAD_RADIUS,
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                color, light);
    }

    @Override
    protected float getU0() {
        return (this.currentCell % GRID) * (1.0F / GRID);
    }

    @Override
    protected float getU1() {
        return (this.currentCell % GRID) * (1.0F / GRID) + (1.0F / GRID);
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F / GRID;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCParticleLayer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return QUAD_RADIUS;
    }

    public static final class Provider implements ParticleProvider<SmokeSpiralData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SmokeSpiralData options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new SmokeSpiralParticle(level, x, y, z, options, this.sprites.first());
        }
    }
}
