package com.leclowndu93150.thaumcraft.client.renderer.particle;

import com.leclowndu93150.thaumcraft.content.particle.FXGenericData;
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
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public final class FXGenericParticle extends SingleQuadParticle {
    private static final float DEG_TO_RAD = (float)(Math.PI / 180.0);

    private final int gridSize;
    private final int startParticle;
    private final int numParticles;
    private final int particleInc;
    private final boolean loop;
    private final int layer;
    private final double slowDownFactor;
    private final double windX;
    private final double windZ;
    private final float rotationSpeedRad;
    private final float randomX;
    private final float randomY;
    private final float randomZ;
    private final int[] finalFrames;
    private final boolean flipped;
    private final boolean angled;
    private final float angleYaw;
    private final float anglePitch;
    private final float rStart;
    private final float gStart;
    private final float bStart;
    private final float rEnd;
    private final float gEnd;
    private final float bEnd;
    private final float[] alphaFrames;
    private final float[] scaleFrames;
    private int currentCell;
    private float prevRoll;
    private float roll;
    private int delayRemaining;
    private final int targetEntityId;
    private Entity targetEntity;

    private FXGenericParticle(ClientLevel level, double x, double y, double z, FXGenericData data, TextureAtlasSprite sprite) {
        super(level, x, y, z, data.vx(), data.vy(), data.vz(), sprite);
        this.setSize(0.1F, 0.1F);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.xd = data.vx();
        this.yd = data.vy();
        this.zd = data.vz();
        this.hasPhysics = false;
        this.gridSize = Math.max(1, data.gridSize());
        this.startParticle = data.startParticle();
        this.numParticles = Math.max(1, data.numParticles());
        this.particleInc = Math.max(1, data.particleInc());
        this.loop = data.loop();
        this.layer = data.layer();
        this.slowDownFactor = data.slowDown();
        this.rotationSpeedRad = data.rotationSpeed() * DEG_TO_RAD;
        this.randomX = data.randomX();
        this.randomY = data.randomY();
        this.randomZ = data.randomZ();
        this.finalFrames = data.finalFrames();
        this.flipped = data.flipped();
        this.angled = data.angled();
        this.angleYaw = data.angleYaw();
        this.anglePitch = data.anglePitch();
        this.rStart = data.rStart();
        this.gStart = data.gStart();
        this.bStart = data.bStart();
        this.rEnd = data.rEnd();
        this.gEnd = data.gEnd();
        this.bEnd = data.bEnd();
        this.rCol = data.rStart();
        this.gCol = data.gStart();
        this.bCol = data.bStart();
        this.lifetime = Math.max(1, data.maxAge());
        this.gravity = data.gravity();
        this.alphaFrames = expandKeys(data.alphaKeys(), this.lifetime);
        this.scaleFrames = expandKeys(data.scaleKeys(), this.lifetime);
        this.delayRemaining = Math.max(0, data.delay());
        this.alpha = this.delayRemaining > 0 ? 0.0F : this.alphaFrames[0];
        this.quadSize = this.scaleFrames[0] * 0.1F;
        this.targetEntityId = data.targetEntityId();
        float startAngleRad = (float)(data.rotationStart() * Math.PI * 2.0);
        this.roll = startAngleRad;
        this.prevRoll = startAngleRad;
        if (data.windScale() != 0.0) {
            int moon = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, new Vec3(x, y, z)).index();
            double angle = moon * (40.0 + level.getRandom().nextInt(10)) / 180.0 * Math.PI;
            double tx = 0.1 * Math.cos(angle);
            double tz = 0.1 * Math.sin(angle);
            this.windX = tx * data.windScale();
            this.windZ = tz * data.windScale();
        } else {
            this.windX = 0.0;
            this.windZ = 0.0;
        }
        updateCell();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRoll = this.roll;
        if (this.delayRemaining > 0) {
            this.delayRemaining--;
            this.alpha = 0.0F;
            return;
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.roll += (float)Math.PI * this.rotationSpeedRad * 2.0F;
        this.yd -= 0.04 * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.slowDownFactor;
        this.yd *= this.slowDownFactor;
        this.zd *= this.slowDownFactor;
        if (this.randomX != 0.0F) this.xd += this.random.nextGaussian() * this.randomX;
        if (this.randomY != 0.0F) this.yd += this.random.nextGaussian() * this.randomY;
        if (this.randomZ != 0.0F) this.zd += this.random.nextGaussian() * this.randomZ;
        this.xd += this.windX;
        this.zd += this.windZ;
        if (this.onGround && this.slowDownFactor != 1.0) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
        if (this.targetEntityId != FXGenericData.Behavior.NO_ENTITY) {
            tickTowardEntity();
        }
        updateCell();
        int safeAge = Mth.clamp(this.age, 0, this.alphaFrames.length - 1);
        this.alpha = this.alphaFrames[safeAge];
        this.quadSize = this.scaleFrames[safeAge] * 0.1F;
        float ageFrac = (float)this.age / this.lifetime;
        this.rCol = this.rStart + (this.rEnd - this.rStart) * ageFrac;
        this.gCol = this.gStart + (this.gEnd - this.gStart) * ageFrac;
        this.bCol = this.bStart + (this.bEnd - this.bStart) * ageFrac;
    }

    private void tickTowardEntity() {
        if (this.targetEntity == null) {
            this.targetEntity = this.level.getEntity(this.targetEntityId);
            if (this.targetEntity == null) return;
        }
        if (!this.targetEntity.isAlive()) {
            this.remove();
            return;
        }
        double dx = this.targetEntity.getX() - this.x;
        double dy = this.targetEntity.getY() - this.y;
        double dz = this.targetEntity.getZ() - this.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1.0E-4) return;
        double accel = 0.3;
        if (dist < 4.0) {
            this.quadSize *= 0.9F;
            accel = 0.6;
        }
        if (dist < 0.25) {
            this.remove();
            return;
        }
        dx /= dist;
        dy /= dist;
        dz /= dist;
        this.xd += dx * accel;
        this.yd += dy * accel;
        this.zd += dz * accel;
        this.xd = Mth.clamp((float)this.xd, -0.35F, 0.35F);
        this.yd = Mth.clamp((float)this.yd, -0.35F, 0.35F);
        this.zd = Mth.clamp((float)this.zd, -0.35F, 0.35F);
    }

    private void updateCell() {
        if (this.loop) {
            this.currentCell = this.startParticle + (this.age / this.particleInc) % this.numParticles;
        } else {
            float fs = (float)this.age / this.lifetime;
            int frame = (int)Math.min(this.numParticles * fs, this.numParticles - 1);
            this.currentCell = this.startParticle + Math.max(0, frame);
        }
        if (this.finalFrames.length > 0 && this.age > this.lifetime - this.finalFrames.length) {
            int idx = this.lifetime - this.age;
            if (idx < 0) idx = 0;
            if (idx >= this.finalFrames.length) idx = this.finalFrames.length - 1;
            this.currentCell = this.finalFrames[idx];
        }
        if (this.currentCell < 0) this.currentCell = 0;
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        Quaternionf rotation = new Quaternionf();
        if (this.angled) {
            rotation.rotationYXZ(-this.angleYaw * DEG_TO_RAD, this.anglePitch * DEG_TO_RAD, 0.0F);
            float r = Mth.lerp(partialTickTime, this.prevRoll, this.roll);
            if (r != 0.0F) rotation.rotateZ(r);
        } else {
            this.getFacingCameraMode().setRotation(rotation, camera, partialTickTime);
            float r = Mth.lerp(partialTickTime, this.prevRoll, this.roll);
            if (r != 0.0F) rotation.rotateZ(r);
        }
        Vec3 pos = camera.position();
        float dx = (float)(Mth.lerp((double)partialTickTime, this.xo, this.x) - pos.x());
        float dy = (float)(Mth.lerp((double)partialTickTime, this.yo, this.y) - pos.y());
        float dz = (float)(Mth.lerp((double)partialTickTime, this.zo, this.z) - pos.z());
        int color = ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);
        int light = this.getLightCoords(partialTickTime);
        renderState.add(
                this.getLayer(),
                dx, dy, dz,
                rotation.x, rotation.y, rotation.z, rotation.w,
                this.getQuadSize(partialTickTime),
                this.getU0(), this.getU1(), this.getV0(), this.getV1(),
                color, light);
    }

    @Override
    protected float getU0() {
        float cellW = 1.0F / this.gridSize;
        int cellX = this.currentCell % this.gridSize;
        float u0 = cellX * cellW;
        return this.flipped ? u0 + cellW : u0;
    }

    @Override
    protected float getU1() {
        float cellW = 1.0F / this.gridSize;
        int cellX = this.currentCell % this.gridSize;
        float u0 = cellX * cellW;
        return this.flipped ? u0 : u0 + cellW;
    }

    @Override
    protected float getV0() {
        float cellH = 1.0F / this.gridSize;
        int cellY = this.currentCell / this.gridSize;
        return cellY * cellH;
    }

    @Override
    protected float getV1() {
        float cellH = 1.0F / this.gridSize;
        int cellY = this.currentCell / this.gridSize;
        return cellY * cellH + cellH;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCParticleLayer.byLayer(this.layer);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize;
    }

    private static float[] expandKeys(float[] keys, int maxAge) {
        if (keys.length == 0) {
            float[] out = new float[maxAge + 1];
            for (int i = 0; i <= maxAge; i++) out[i] = 1.0F;
            return out;
        }
        if (keys.length == 1) {
            float[] out = new float[maxAge + 1];
            for (int i = 0; i <= maxAge; i++) out[i] = keys[0];
            return out;
        }
        float[] out = new float[maxAge + 1];
        float inc = (float)(keys.length - 1) / maxAge;
        float pos = 0.0F;
        for (int a = 0; a <= maxAge; a++) {
            int idx = Mth.floor(pos);
            float diff = idx < keys.length - 1 ? keys[idx + 1] - keys[idx] : 0.0F;
            float frac = pos - idx;
            out[a] = keys[idx] + diff * frac;
            pos += inc;
        }
        return out;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Provider implements ParticleProvider<FXGenericData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(FXGenericData options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new FXGenericParticle(level, x, y, z, options, this.sprites.first());
        }
    }
}
