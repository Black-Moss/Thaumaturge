package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import com.leclowndu93150.thaumcraft.client.fx.render.texture.TCParticleLayer;
import com.leclowndu93150.thaumcraft.content.fx.data.VisSparkleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class VisSparkleParticle extends SingleQuadParticle {
    private static final int GRID_SIZE = 64;
    private static final int FRAME_ROW_START = 512;
    private static final int FRAME_COUNT = 16;
    private static final int FIXED_LIFETIME = 1000;
    private static final int SCALE_RAMP_AGE = 10;
    private static final float HOMING_ACCEL = 0.1F;
    private static final float SPEED_CAP = 0.1F;

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float sizeMod;
    private float scale;

    private VisSparkleParticle(ClientLevel level, double x, double y, double z, VisSparkleData data, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprites.first());
        if (data.color() >= 0) {
            this.rCol = ((data.color() >> 16) & 0xFF) / 255.0F;
            this.gCol = ((data.color() >> 8) & 0xFF) / 255.0F;
            this.bCol = (data.color() & 0xFF) / 255.0F;
        } else {
            this.rCol = 0.2F;
            this.gCol = 0.6F + level.getRandom().nextFloat() * 0.3F;
            this.bCol = 0.2F;
        }
        this.alpha = 0.5F;
        this.scale = 0.0F;
        this.targetX = data.tx();
        this.targetY = data.ty();
        this.targetZ = data.tz();
        this.lifetime = FIXED_LIFETIME;
        float spread = 0.01F;
        this.xd = (float) level.getRandom().nextGaussian() * spread;
        this.yd = (float) level.getRandom().nextGaussian() * spread;
        this.zd = (float) level.getRandom().nextGaussian() * spread;
        this.sizeMod = 45 + level.getRandom().nextInt(15);
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
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 2.0) {
            this.scale *= 0.95F;
        }
        if (distance < 0.2) {
            this.remove();
            return;
        }
        if (this.age < SCALE_RAMP_AGE) {
            this.scale = this.age / this.sizeMod;
        }
        dx /= distance;
        dy /= distance;
        dz /= distance;
        this.xd += dx * HOMING_ACCEL;
        this.yd += dy * HOMING_ACCEL;
        this.zd += dz * HOMING_ACCEL;
        this.xd = Mth.clamp((float) this.xd, -SPEED_CAP, SPEED_CAP);
        this.yd = Mth.clamp((float) this.yd, -SPEED_CAP, SPEED_CAP);
        this.zd = Mth.clamp((float) this.zd, -SPEED_CAP, SPEED_CAP);
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float bob = Mth.sin(this.age / 3.0F) * 0.3F + 6.0F;
        return 0.1F * this.scale * bob;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    private int currentCell() {
        return FRAME_ROW_START + this.age % FRAME_COUNT;
    }

    @Override
    protected float getU0() {
        return (currentCell() % GRID_SIZE) / (float) GRID_SIZE;
    }

    @Override
    protected float getU1() {
        return getU0() + 1.0F / GRID_SIZE;
    }

    @Override
    protected float getV0() {
        return (currentCell() / GRID_SIZE) / (float) GRID_SIZE;
    }

    @Override
    protected float getV1() {
        return getV0() + 1.0F / GRID_SIZE;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCParticleLayer.byLayer(0);
    }

    public static final class Provider implements ParticleProvider<VisSparkleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(VisSparkleData options, ClientLevel level, double x, double y, double z,
                                       double xa, double ya, double za, RandomSource random) {
            return new VisSparkleParticle(level, x, y, z, options, this.sprites);
        }
    }
}
