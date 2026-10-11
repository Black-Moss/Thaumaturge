package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.instance.beam.BeamAnchor;
import com.leclowndu93150.thaumaturge.client.effect.instance.beam.BeamPose;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class BeamInstance implements IFXInstance {
    private static final float CHANNEL_SCALE = 255.0F;
    private static final int SPIN_DEGREES_PER_TICK = 5;
    private static final float FULL_CIRCLE_DEGREES = 360.0F;
    private static final float SCROLL_RATE = 0.06F;
    private static final long SCROLL_PERIOD_TICKS = 1000L;
    private static final float GROW_DURATION = 4.0F;
    private static final float FADE_DURATION = 5.0F;
    private static final float PEAK_OPACITY = 0.38F;
    private static final double SAME_SOURCE_DISTANCE_SQR = 0.01;

    private final float[] tint;
    private final int beamType;
    private final float endMod;
    private final boolean reverse;
    private final int sourceEntityId;
    private final boolean withSource;
    private final Vec3 requestedSource;

    private Vec3 target;
    private int maxAge;
    private BeamPose latest;
    private BeamPose earlier;
    private int age;
    private boolean expired;

    public BeamInstance(double sourceX, double sourceY, double sourceZ, double targetX, double targetY, double targetZ, int color, int maxAge, int beamType, float endMod, boolean reverse, int sourceEntityId, boolean withSource) {
        this.target = new Vec3(targetX, targetY, targetZ);
        this.tint = new float[]{ARGB.red(color) / CHANNEL_SCALE, ARGB.green(color) / CHANNEL_SCALE, ARGB.blue(color) / CHANNEL_SCALE};
        this.maxAge = maxAge;
        this.beamType = beamType;
        this.endMod = endMod;
        this.reverse = reverse;
        this.sourceEntityId = sourceEntityId;
        this.withSource = withSource;
        this.requestedSource = new Vec3(sourceX, sourceY, sourceZ);
        this.latest = BeamPose.between(this.requestedSource, this.target);
        this.earlier = this.latest;
    }

    @Override
    public void tick() {
        LivingEntity anchor = BeamAnchor.find(this.sourceEntityId);
        this.earlier = this.latest;
        this.latest = BeamPose.between(anchor != null ? BeamAnchor.castPoint(anchor, 1.0F) : this.earlier.source(), this.target);
        this.expired = this.age >= this.maxAge;
        if (!this.expired) {
            this.age++;
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public boolean sameSourceAs(BeamInstance other) {
        if (this.beamType != other.beamType || this.withSource != other.withSource) {
            return false;
        }
        if (entityAnchored() || other.entityAnchored()) {
            return this.sourceEntityId == other.sourceEntityId;
        }
        return this.requestedSource.distanceToSqr(other.requestedSource) < SAME_SOURCE_DISTANCE_SQR;
    }

    public void refresh(BeamInstance newer) {
        this.target = newer.target;
        this.maxAge = Math.max(this.maxAge, this.age + newer.maxAge);
        this.expired = false;
    }

    public float computeSize(float partialTick) {
        return ramp((this.age + partialTick) / GROW_DURATION);
    }

    public float computeOpacity(float partialTick) {
        return PEAK_OPACITY * fadeFactor(partialTick);
    }

    public float fadeFactor(float partialTick) {
        float remaining = this.maxAge - (this.age + partialTick);
        return ramp(remaining / FADE_DURATION);
    }

    public float texScroll(float partialTick) {
        float ticks = (worldTime() % SCROLL_PERIOD_TICKS) + partialTick;
        float scroll = ticks * SCROLL_RATE;
        return this.reverse ? scroll : -scroll;
    }

    public float worldRotation(float partialTick) {
        long period = (long) (FULL_CIRCLE_DEGREES / SPIN_DEGREES_PER_TICK);
        float ticks = (worldTime() % period) + partialTick;
        return ticks * SPIN_DEGREES_PER_TICK % FULL_CIRCLE_DEGREES;
    }

    private static float ramp(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }

    private static long worldTime() {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? level.getGameTime() : 0L;
    }

    public int beamType() {
        return this.beamType;
    }

    public float length() {
        return (float) this.latest.source().distanceTo(this.target);
    }

    public float endMod() {
        return this.endMod;
    }

    public boolean reverse() {
        return this.reverse;
    }

    public int rotationSpeed() {
        return SPIN_DEGREES_PER_TICK;
    }

    public int age() {
        return this.age;
    }

    public int maxAge() {
        return this.maxAge;
    }

    public float colorR() {
        return this.tint[0];
    }

    public float colorG() {
        return this.tint[1];
    }

    public float colorB() {
        return this.tint[2];
    }

    public boolean entityAnchored() {
        return this.sourceEntityId != BeamPayloadIds.NO_ENTITY;
    }

    public boolean withSource() {
        return this.withSource;
    }

    public Vec3 sourcePos(float partialTick) {
        LivingEntity anchor = BeamAnchor.find(this.sourceEntityId);
        return anchor != null ? BeamAnchor.castPoint(anchor, partialTick) : this.earlier.source().lerp(this.latest.source(), partialTick);
    }

    public Vec3 targetPos(float partialTick) {
        return this.target;
    }

    public float yawAt(float partialTick) {
        return Mth.rotLerp(partialTick, this.earlier.yaw(), this.latest.yaw());
    }

    public float pitchAt(float partialTick) {
        return Mth.rotLerp(partialTick, this.earlier.pitch(), this.latest.pitch());
    }

}
