package com.leclowndu93150.thaumcraft.client.fx.render.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public abstract class HomingParticleBase extends SingleQuadParticle {
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected final float driftScale;
    private final int targetEntityId;
    private Entity targetEntity;

    protected HomingParticleBase(ClientLevel level, double x, double y, double z,
                                 int targetEntityId, double tx, double ty, double tz,
                                 double sx, double sy, double sz, float minScale, float scaleRange,
                                 float gravity, float driftScale, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.targetEntityId = targetEntityId;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.driftScale = driftScale;
        this.quadSize = this.random.nextFloat() * scaleRange + minScale;
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 10.0F);
        if (base < 1) {
            base = 1;
        }
        this.lifetime = base / 2 + this.random.nextInt(base);
        this.xd = sx + this.random.nextGaussian() * 0.01;
        this.yd = sy + this.random.nextGaussian() * 0.01;
        this.zd = sz + this.random.nextGaussian() * 0.01;
        this.gravity = gravity;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        updateTarget();
        int floorX = Mth.floor(this.x);
        int floorY = Mth.floor(this.y);
        int floorZ = Mth.floor(this.z);
        int tFloorX = Mth.floor(this.targetX);
        int tFloorY = Mth.floor(this.targetY);
        int tFloorZ = Mth.floor(this.targetZ);
        boolean reachedTarget = floorX == tFloorX && floorY == tFloorY && floorZ == tFloorZ;
        if (this.age++ >= this.lifetime || reachedTarget) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.985;
        this.yd *= 0.95;
        this.zd *= 0.985;
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        double clamp = Math.min(0.25, dist / 15.0);
        if (dist < 2.0) {
            this.quadSize *= 0.9F;
        }
        if (dist > 1.0E-6) {
            dx /= dist;
            dy /= dist;
            dz /= dist;
        }
        this.xd += dx * clamp;
        this.yd += dy * clamp;
        this.zd += dz * clamp;
        this.xd = Mth.clamp(this.xd, -clamp, clamp);
        this.yd = Mth.clamp(this.yd, -clamp, clamp);
        this.zd = Mth.clamp(this.zd, -clamp, clamp);
        this.xd += this.random.nextGaussian() * this.driftScale;
        this.yd += this.random.nextGaussian() * this.driftScale;
        this.zd += this.random.nextGaussian() * this.driftScale;
    }

    private void updateTarget() {
        if (this.targetEntityId < 0) {
            return;
        }
        if (this.targetEntity == null) {
            this.targetEntity = this.level.getEntity(this.targetEntityId);
        }
        if (this.targetEntity != null) {
            this.targetX = this.targetEntity.getX();
            this.targetY = this.targetEntity.getEyeY();
            this.targetZ = this.targetEntity.getZ();
        }
    }
}
