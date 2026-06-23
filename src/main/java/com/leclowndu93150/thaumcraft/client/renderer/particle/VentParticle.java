package com.leclowndu93150.thaumcraft.client.renderer.particle;

import com.leclowndu93150.thaumcraft.content.particle.VentData;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public final class VentParticle extends SingleQuadParticle {
    private static final int GRID = 64;
    private static final float GROW_VENT = 1.15F;
    private static final float GROW_VENT2 = 1.2F;
    private static final float ALPHA = 0.4F;
    private static final float SCALE_HEADING = 0.125F;
    private static final float SCALE_JITTER = 5.0F;

    private final boolean variant;
    private final float psm;
    private float scale;
    private final float perParticleGravity;

    private VentParticle(ClientLevel level, double x, double y, double z, VentData data, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.setSize(0.02F, 0.02F);
        this.variant = data.variant();
        float initialScale = this.random.nextFloat() * 0.1F + 0.05F;
        this.scale = initialScale * data.scale();
        this.psm = data.scale();
        this.xd = data.vx();
        this.yd = data.vy();
        this.zd = data.vz();
        int color = data.color();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        if (this.variant) {
            this.rCol = (float)Mth.clamp(r + this.random.nextGaussian() * 0.05, 0.0, 1.0);
            this.gCol = (float)Mth.clamp(g + this.random.nextGaussian() * 0.05, 0.0, 1.0);
            this.bCol = (float)Mth.clamp(b + this.random.nextGaussian() * 0.05, 0.0, 1.0);
            this.perParticleGravity = (float)(this.random.nextGaussian() * 0.0075);
        } else {
            this.rCol = r;
            this.gCol = g;
            this.bCol = b;
            this.perParticleGravity = 0.0025F;
            setHeading(data.vx(), data.vy(), data.vz());
        }
        this.lifetime = Integer.MAX_VALUE;
        this.hasPhysics = true;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.position();
        int visibleDistance = Minecraft.getInstance().options.graphicsPreset().get() == GraphicsPreset.FAST ? 25 : 50;
        double dx = camPos.x() - x;
        double dy = camPos.y() - y;
        double dz = camPos.z() - z;
        if (dx * dx + dy * dy + dz * dz > visibleDistance * visibleDistance) {
            this.remove();
        }
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    private void setHeading(double vxIn, double vyIn, double vzIn) {
        double len = Math.sqrt(vxIn * vxIn + vyIn * vyIn + vzIn * vzIn);
        if (len < 1.0E-6) return;
        double nx = vxIn / len;
        double ny = vyIn / len;
        double nz = vzIn / len;
        nx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * SCALE_JITTER;
        ny += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * SCALE_JITTER;
        nz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * SCALE_JITTER;
        this.xd = nx * SCALE_HEADING;
        this.yd = ny * SCALE_HEADING;
        this.zd = nz * SCALE_HEADING;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;
        if (this.scale >= this.psm) {
            this.remove();
            return;
        }
        this.yd += this.perParticleGravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;
        float growRate = this.variant ? GROW_VENT2 : GROW_VENT;
        if (this.scale < this.psm) {
            this.scale = this.scale * growRate;
        }
        if (this.scale > this.psm) {
            this.scale = this.psm;
        }
        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partialTickTime);
        Vec3 pos = camera.position();
        float dx = (float)(Mth.lerp((double)partialTickTime, this.xo, this.x) - pos.x());
        float dy = (float)(Mth.lerp((double)partialTickTime, this.yo, this.y) - pos.y());
        float dz = (float)(Mth.lerp((double)partialTickTime, this.zo, this.z) - pos.z());
        float fadeAlpha = ALPHA * ((this.psm - this.scale) / this.psm);
        int color = ARGB.colorFromFloat(fadeAlpha, this.rCol, this.gCol, this.bCol);
        int light = this.getLightCoords(partialTickTime);
        renderState.add(
                this.getLayer(),
                dx, dy, dz,
                rotation.x, rotation.y, rotation.z, rotation.w,
                0.3F * this.scale,
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                color, light);
    }

    private int cellIndex() {
        int part = (int)(1.0F + this.scale / this.psm * 4.0F);
        if (part < 0) return 0;
        if (part > 5) return 5;
        return part;
    }

    @Override
    protected float getU0() {
        return cellIndex() * (1.0F / GRID);
    }

    @Override
    protected float getU1() {
        return cellIndex() * (1.0F / GRID) + (1.0F / GRID);
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
        return 0.3F * this.scale;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Provider implements ParticleProvider<VentData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(VentData options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new VentParticle(level, x, y, z, options, this.sprites.first());
        }
    }
}
