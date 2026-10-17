package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.instance.beam.BeamAnchor;
import com.leclowndu93150.thaumaturge.client.effect.instance.beam.BeamPose;
import com.leclowndu93150.thaumaturge.client.effect.instance.beam.BeamSourceKey;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class BeamInstance implements IFXInstance {
    private static final float CHANNEL_SCALE = 255.0F;
    private static final int SPIN_DEGREES_PER_TICK = 5;
    private static final int FULL_CIRCLE_DEGREES = 360;
    private static final float GROW_DURATION = 4.0F;
    private static final float FADE_DURATION = 4.0F;
    private static final float PEAK_OPACITY = 0.4F;
    private static final float SCROLL_RATE = 0.2F;
    private static final float SCROLL_PERIOD_RATE = 0.1F;
    private static final int FLARE_TICKS = 6;

    private final BeamSourceKey sourceKey;
    private final float[] tint;
    private final float endMod;
    private final boolean reverse;

    private Vec3 target;
    private int maxAge;
    private int flareTicks;
    private BeamPose latest;
    private BeamPose earlier;
    private int age;
    private boolean expired;

    public BeamInstance(double sourceX, double sourceY, double sourceZ, double targetX, double targetY, double targetZ, int color, int maxAge, int beamType, float endMod, boolean reverse, int sourceEntityId, boolean withSource, boolean struck) {
        this.target = new Vec3(targetX, targetY, targetZ);
        this.tint = new float[]{ARGB.red(color) / CHANNEL_SCALE, ARGB.green(color) / CHANNEL_SCALE, ARGB.blue(color) / CHANNEL_SCALE};
        this.maxAge = maxAge;
        this.endMod = endMod;
        this.reverse = reverse;
        this.flareTicks = struck ? FLARE_TICKS : 0;
        Vec3 launch = new Vec3(sourceX, sourceY, sourceZ);
        this.sourceKey = new BeamSourceKey(beamType, withSource, sourceEntityId, launch);
        this.latest = BeamPose.between(launch, this.target);
        this.earlier = this.latest;
    }

    @Override
    public void tick() {
        if (this.flareTicks > 0) {
            this.flareTicks--;
        }
        this.earlier = this.latest;
        this.latest = poseFrom(this.earlier.source());
        boolean finished = this.age >= this.maxAge;
        this.expired = finished && this.flareTicks == 0;
        if (!finished) {
            this.age++;
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public boolean mergesWith(BeamInstance other) {
        return this.sourceKey.matches(other.sourceKey);
    }

    public void absorb(BeamInstance newer) {
        this.target = newer.target;
        this.maxAge = Math.max(this.maxAge, this.age + newer.maxAge);
        this.flareTicks = Math.max(this.flareTicks, newer.flareTicks);
        this.expired = false;
        this.latest = poseFrom(this.latest.source());
    }

    public boolean flareActive() {
        return this.flareTicks > 0;
    }

    public float flareFraction(float partialTick) {
        return ramp(this.flareTicks - partialTick, FLARE_TICKS);
    }

    public int beamType() {
        return this.sourceKey.beamType();
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
        return this.sourceKey.anchorEntityId() != BeamPayloadIds.NO_ENTITY;
    }

    public boolean withSource() {
        return this.sourceKey.withSource();
    }

    public Vec3 sourcePos(float partialTick) {
        LivingEntity anchor = BeamAnchor.find(this.sourceKey.anchorEntityId());
        return anchor != null ? BeamAnchor.handSpan(anchor, partialTick).lerp(partialTick) : this.earlier.source().lerp(this.latest.source(), partialTick);
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

    public float computeSize(float partialTick) {
        return ramp(this.age + partialTick, GROW_DURATION);
    }

    public float computeOpacity(float partialTick) {
        return PEAK_OPACITY * ramp(this.maxAge - this.age, FADE_DURATION);
    }

    public float texScroll(float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        float elapsed = (player == null ? 0 : player.tickCount) + partialTick;
        float flow = this.reverse ? elapsed : -elapsed;
        return flow * SCROLL_RATE - (float) Math.floor(flow * SCROLL_PERIOD_RATE);
    }

    public float worldRotation(float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        long clock = level == null ? 0L : level.getGameTime();
        long wrapped = clock % (FULL_CIRCLE_DEGREES / SPIN_DEGREES_PER_TICK);
        return wrapped * SPIN_DEGREES_PER_TICK + SPIN_DEGREES_PER_TICK * partialTick;
    }

    private BeamPose poseFrom(Vec3 fallbackSource) {
        LivingEntity anchor = BeamAnchor.find(this.sourceKey.anchorEntityId());
        return BeamPose.between(anchor != null ? BeamAnchor.chestPoint(anchor) : fallbackSource, this.target);
    }

    private static float ramp(float value, float duration) {
        return Mth.clamp(value / duration, 0.0F, 1.0F);
    }
}
