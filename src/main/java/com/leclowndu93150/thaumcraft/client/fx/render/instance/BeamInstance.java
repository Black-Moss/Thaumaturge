package com.leclowndu93150.thaumcraft.client.fx.render.instance;

import com.leclowndu93150.thaumcraft.client.fx.render.manager.IFXInstance;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BeamInstance implements IFXInstance {
    private final int sourceEntityId;
    private final boolean entityAnchored;
    private final boolean withSource;
    private final int beamType;
    private double sourceX;
    private double sourceY;
    private double sourceZ;
    private double prevSourceX;
    private double prevSourceY;
    private double prevSourceZ;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double prevTargetX;
    private double prevTargetY;
    private double prevTargetZ;
    private final float colorR;
    private final float colorG;
    private final float colorB;
    private final float endMod;
    private final boolean reverse;
    private final int rotationSpeed;
    private float length;
    private float rotYaw;
    private float rotPitch;
    private float prevYaw;
    private float prevPitch;
    private int age = 0;
    private int maxAge;
    private float prevSize = 0.0F;
    private boolean expired = false;

    public BeamInstance(double sx, double sy, double sz, double tx, double ty, double tz,
                        int color, int age, int beamType, float endMod, boolean reverse,
                        int sourceEntityId, boolean withSource) {
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.maxAge = age;
        this.beamType = beamType;
        this.endMod = endMod;
        this.reverse = reverse;
        this.rotationSpeed = 5;
        this.sourceEntityId = sourceEntityId;
        this.entityAnchored = sourceEntityId != BeamPayloadIds.NO_ENTITY;
        this.withSource = withSource;
        this.sourceX = sx;
        this.sourceY = sy;
        this.sourceZ = sz;
        this.prevSourceX = sx;
        this.prevSourceY = sy;
        this.prevSourceZ = sz;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.prevTargetX = tx;
        this.prevTargetY = ty;
        this.prevTargetZ = tz;
        recomputeOrientation();
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
    }

    public void tick() {
        this.prevSize = computeSize(0.0F);
        this.prevSourceX = this.sourceX;
        this.prevSourceY = this.sourceY;
        this.prevSourceZ = this.sourceZ;
        this.prevTargetX = this.targetX;
        this.prevTargetY = this.targetY;
        this.prevTargetZ = this.targetZ;
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
        if (this.entityAnchored) {
            Entity entity = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(this.sourceEntityId) : null;
            if (entity != null && entity.isAlive()) {
                double offset = entity.getBbHeight() / 2.0 + 0.25;
                this.sourceX = entity.getX();
                this.sourceY = entity.getY() + offset;
                this.sourceZ = entity.getZ();
            }
        }
        recomputeOrientation();
        normalizeAngleDelta();
        if (this.age++ >= this.maxAge) {
            this.expired = true;
        }
    }

    private void recomputeOrientation() {
        float xd = (float)(this.sourceX - this.targetX);
        float yd = (float)(this.sourceY - this.targetY);
        float zd = (float)(this.sourceZ - this.targetZ);
        this.length = Mth.sqrt(xd * xd + yd * yd + zd * zd);
        double horiz = Mth.sqrt(xd * xd + zd * zd);
        this.rotYaw = (float)(Math.atan2(xd, zd) * 180.0 / Math.PI);
        this.rotPitch = (float)(Math.atan2(yd, horiz) * 180.0 / Math.PI);
    }

    private void normalizeAngleDelta() {
        while (this.rotPitch - this.prevPitch < -180.0F) this.prevPitch -= 360.0F;
        while (this.rotPitch - this.prevPitch >= 180.0F) this.prevPitch += 360.0F;
        while (this.rotYaw - this.prevYaw < -180.0F) this.prevYaw -= 360.0F;
        while (this.rotYaw - this.prevYaw >= 180.0F) this.prevYaw += 360.0F;
    }

    public boolean isExpired() { return this.expired; }
    public int beamType() { return this.beamType; }
    public float length() { return this.length; }
    public float endMod() { return this.endMod; }
    public boolean reverse() { return this.reverse; }
    public int rotationSpeed() { return this.rotationSpeed; }
    public int age() { return this.age; }
    public int maxAge() { return this.maxAge; }
    public float colorR() { return this.colorR; }
    public float colorG() { return this.colorG; }
    public float colorB() { return this.colorB; }
    public boolean entityAnchored() { return this.entityAnchored; }
    public boolean withSource() { return this.withSource; }

    public Vec3 sourcePos(float partial) {
        if (this.entityAnchored) {
            Entity entity = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(this.sourceEntityId) : null;
            if (entity != null && entity.isAlive()) {
                double offset = entity.getBbHeight() / 2.0 + 0.25;
                double yawRad = Mth.lerp(partial, entity.yRotO, entity.getYRot()) / 180.0 * Math.PI;
                double prevYawRad = entity.yRotO / 180.0 * Math.PI;
                double prex = entity.xOld;
                double prey = entity.yOld + offset;
                double prez = entity.zOld;
                prex -= Math.cos(prevYawRad) * 0.066;
                prey -= 0.06;
                prez -= Math.sin(prevYawRad) * 0.04;
                double px = entity.getX();
                double py = entity.getY() + offset;
                double pz = entity.getZ();
                px -= Math.cos(yawRad) * 0.066;
                py -= 0.06;
                pz -= Math.sin(yawRad) * 0.04;
                Vec3 look = entity.getViewVector(1.0F);
                prex += look.x * 0.3;
                prey += look.y * 0.3;
                prez += look.z * 0.3;
                px += look.x * 0.3;
                py += look.y * 0.3;
                pz += look.z * 0.3;
                double ix = Mth.lerp(partial, prex, px);
                double iy = Mth.lerp(partial, prey, py);
                double iz = Mth.lerp(partial, prez, pz);
                return new Vec3(ix, iy, iz);
            }
        }
        double ix = Mth.lerp(partial, this.prevSourceX, this.sourceX);
        double iy = Mth.lerp(partial, this.prevSourceY, this.sourceY);
        double iz = Mth.lerp(partial, this.prevSourceZ, this.sourceZ);
        return new Vec3(ix, iy, iz);
    }

    public Vec3 targetPos(float partial) {
        double ix = Mth.lerp(partial, this.prevTargetX, this.targetX);
        double iy = Mth.lerp(partial, this.prevTargetY, this.targetY);
        double iz = Mth.lerp(partial, this.prevTargetZ, this.targetZ);
        return new Vec3(ix, iy, iz);
    }

    public float yawAt(float partial) {
        return Mth.lerp(partial, this.prevYaw, this.rotYaw);
    }

    public float pitchAt(float partial) {
        return Mth.lerp(partial, this.prevPitch, this.rotPitch);
    }

    public float computeSize(float partial) {
        float size = Math.min((this.age + partial) / 4.0F, 1.0F);
        return Mth.lerp(partial, this.prevSize, size);
    }

    public float computeOpacity(float partial) {
        float op = 0.4F;
        if (this.maxAge - this.age <= 4) {
            op = 0.4F - (4 - (this.maxAge - this.age)) * 0.1F;
        }
        return op;
    }

    public float texScroll(float partial) {
        LocalPlayer player = Minecraft.getInstance().player;
        float slide = player != null ? player.tickCount : 0;
        float v = slide + partial;
        if (this.reverse) v *= -1.0F;
        return -v * 0.2F - Mth.floor(-v * 0.1F);
    }

    public float worldRotation(float partial) {
        long worldTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        return (float)(worldTime % (360 / this.rotationSpeed) * this.rotationSpeed) + this.rotationSpeed * partial;
    }
}
