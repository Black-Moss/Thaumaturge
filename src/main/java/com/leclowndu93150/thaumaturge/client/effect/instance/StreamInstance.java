package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.instance.stream.DirectedLaunch;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.HeadMotion;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.LaunchRule;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.RangeShrink;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.ShrinkRule;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.SizingResult;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.SnapshotRequest;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.StreamFrame;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.StreamSnapshotBuilder;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.StreamStep;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.TrailBuffer;
import com.leclowndu93150.thaumaturge.client.effect.instance.stream.WobbleLaunch;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class StreamInstance implements IFXInstance {
    protected static final double MIN_RADIUS = 0.001;

    private static final float CHANNEL_SCALE = 255.0F;
    private static final float RADIUS_SCATTER = 0.15F;
    private static final double ARRIVAL_EPSILON = 0.000001;
    private static final double PULSE_FLOOR = 0.75;
    private static final double PULSE_DEPTH = 0.25;
    private static final double PULSE_DIVISOR = 2.0;
    private static final double TICKS_PER_BLOCK = 21.0;
    private static final int SEED_TRAIL_POINTS = 2;
    private static final double SHIMMER_DIVISOR = 2.0;
    private static final double SHIMMER_DEPTH = 0.1;
    private static final float OPAQUE_ALPHA = 1.0F;
    private static final double DEFAULT_SHRINK_RANGE = 1.0;

    protected final double originX;
    protected final double originY;
    protected final double originZ;
    protected final float colorR;
    protected final float colorG;
    protected final float colorB;
    protected final int waveSeed;
    protected final List<TrailPoint> trail;
    protected float radiusBase;
    protected int maxAge;
    protected int length;
    protected int age;

    private final TrailBuffer trailBuffer;
    private final HeadMotion head;
    private final StreamStep step = new StreamStep();
    private boolean targetMissing;

    protected StreamInstance(double x, double y, double z, int color, int count, float scale) {
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.head = new HeadMotion(x, y, z);
        this.colorR = ARGB.red(color) / CHANNEL_SCALE;
        this.colorG = ARGB.green(color) / CHANNEL_SCALE;
        this.colorB = ARGB.blue(color) / CHANNEL_SCALE;
        this.waveSeed = count;
        this.radiusBase = scale * (1.0F + (float) currentRandom().nextGaussian() * RADIUS_SCATTER);
        this.trailBuffer = new TrailBuffer(SEED_TRAIL_POINTS, MIN_RADIUS);
        this.trail = this.trailBuffer.points();
    }

    protected static RandomSource currentRandom() {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? level.getRandom() : RandomSource.create();
    }

    protected final void launch(LaunchRule rule) {
        rule.apply(this.head, this.waveSeed);
    }

    protected final void launchMotion(float amplitude, double verticalBoost) {
        launch(new WobbleLaunch(amplitude, verticalBoost));
    }

    protected final void launchToward(Vec3 direction, double speed, float wobble) {
        launch(new DirectedLaunch(direction, speed, wobble));
    }

    protected final Vec3 position() {
        return this.head.position();
    }

    protected static int travelTicks(double x1, double y1, double z1, double x2, double y2, double z2) {
        double distance = new Vec3(x1, y1, z1).distanceTo(new Vec3(x2, y2, z2));
        return Math.max(1, (int) (distance * TICKS_PER_BLOCK));
    }

    @Override
    public final boolean isExpired() {
        return this.targetMissing || this.age >= this.maxAge || this.length < 1;
    }

    @Override
    public final void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || isExpired()) {
            return;
        }
        Vec3 target = seekTarget(level);
        if (target == null) {
            this.targetMissing = true;
            return;
        }
        advance(target);
        steer(level);
        trailStep(level, size());
        afterStep(level);
        this.age++;
    }

    private void advance(Vec3 target) {
        this.head.advance();
        this.step.measure(target, this.head);
    }

    private void steer(ClientLevel level) {
        double distance = this.step.distance();
        restrainMotion(distance);
        if (distance > ARRIVAL_EPSILON) {
            double pull = pullStrength(distance) / Math.min(1.0, distance);
            double jitterX = steerJitter(level);
            double jitterY = steerJitter(level);
            double jitterZ = steerJitter(level);
            this.head.accelerate(this.step.directionX() * pull + jitterX, this.step.directionY() * pull + jitterY, this.step.directionZ() * pull + jitterZ);
        }
    }

    private SizingResult size() {
        double distance = this.step.distance();
        double pointRadius = this.radiusBase * (PULSE_FLOOR + PULSE_DEPTH * Math.sin((this.waveSeed + this.age) / PULSE_DIVISOR));
        float baseRadius = this.radiusBase;
        ShrinkRule shrink = shrinkRule();
        if (shrink.applies(distance)) {
            double narrowing = shrink.factor(distance);
            pointRadius *= narrowing;
            baseRadius = (float) (baseRadius * narrowing);
        }
        return new SizingResult(pointRadius, baseRadius);
    }

    private void trailStep(ClientLevel level, SizingResult sizing) {
        this.radiusBase = sizing.baseRadius();
        if (this.radiusBase > MIN_RADIUS) {
            this.trailBuffer.append(new TrailPoint(sizing.pointRadius(), this.head.x() - this.originX, this.head.y() - this.originY, this.head.z() - this.originZ));
        } else {
            this.trailBuffer.markDissolveStart(this.age);
            this.length--;
            onDissolve(level);
        }
        this.trailBuffer.trimTo(this.length);
    }

    protected final void clampMotion(double limit) {
        this.head.clamp(limit);
    }

    protected abstract @Nullable Vec3 seekTarget(ClientLevel level);

    protected abstract void restrainMotion(double distance);

    protected abstract double pullStrength(double distance);

    protected double steerJitter(ClientLevel level) {
        return 0.0;
    }

    protected double shrinkRange() {
        return DEFAULT_SHRINK_RANGE;
    }

    protected ShrinkRule shrinkRule() {
        return new RangeShrink(shrinkRange());
    }

    protected void onDissolve(ClientLevel level) {}

    protected void afterStep(ClientLevel level) {}

    protected final @Nullable Snapshot buildSnapshot(float partialTick, float wobble, boolean newestFirst, float radiusMultiplier) {
        StreamFrame frame = new StreamFrame(this.age, this.radiusBase, this.originX, this.originY, this.originZ);
        SnapshotRequest request = new SnapshotRequest(partialTick, wobble, newestFirst, radiusMultiplier);
        return StreamSnapshotBuilder.build(this.trailBuffer, frame, request, this::colour);
    }

    protected void colour(float[] out, int slot) {
        double dim = 1.0 - SHIMMER_DEPTH * Math.sin((slot + this.age) / SHIMMER_DIVISOR);
        out[0] = (float) Math.min(1.0, this.colorR * dim);
        out[1] = (float) Math.min(1.0, this.colorG * dim);
        out[2] = (float) Math.min(1.0, this.colorB * dim);
        out[3] = OPAQUE_ALPHA;
    }

    protected final TrailPoint trailPoint(int index) {
        return this.trailBuffer.get(index);
    }

    protected final int trailSize() {
        return this.trailBuffer.size();
    }

    public record Snapshot(double[][] points, float[][] colours, double[] radii, double originX, double originY, double originZ, float texSlice, float start) {
    }

    public record TrailPoint(double radius, double x, double y, double z) {
    }
}
