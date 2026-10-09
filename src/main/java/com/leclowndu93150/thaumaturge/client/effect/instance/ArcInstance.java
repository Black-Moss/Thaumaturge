package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ArcInstance implements IFXInstance {
    static final int MAX_AGE = 3;

    private static final float CHANNEL_MAX = 255.0F;
    private static final double MIN_HINT = 0.1;
    private static final double LAUNCH_GRAVITY = 0.115;
    private static final double DROP_DIVISOR = 1.9;
    private static final double SAMPLE_DROP = LAUNCH_GRAVITY / DROP_DIVISOR;
    private static final double JITTER = 0.25;
    private static final int MAX_SAMPLES = 50;
    private static final double VERTICAL_RUN_EPSILON = 1.0E-9;
    private static final double SLOPE_FACTOR = 2.0;
    private static final float CRACKLE_BRIGHTEN = 3.0F;
    private static final float CRACKLE_SCALE = 0.5F;
    private static final int CRACKLE_DELAY_BASE = 2;
    private static final int CRACKLE_DELAY_RANGE = 3;
    private static final float CRACKLE_DECAY = 0.995F;
    private static final float CRACKLE_GRAVITY = 0.1F;
    private static final int CRACKLE_BASE_AGE = 8;

    public final Vec3 origin;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float chordLength;
    public final List<Vec3> path;

    private int age;
    private boolean expired;

    public ArcInstance(double startX, double startY, double startZ, double targetX, double targetY, double targetZ, int color, float hint) {
        Vec3 span = new Vec3(targetX, targetY, targetZ).subtract(startX, startY, startZ);
        RandomSource random = StreamInstance.currentRandom();
        double arch = hint > 0.0F ? hint : MIN_HINT;
        this.origin = new Vec3(startX, startY, startZ);
        this.colorR = channel(ARGB.red(color));
        this.colorG = channel(ARGB.green(color));
        this.colorB = channel(ARGB.blue(color));
        this.chordLength = (float) span.length();
        this.path = trace(Trajectory.towards(span, arch), span, random);
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            this.crackleAlong(level, random);
        }
    }

    private static float channel(int value) {
        return value / CHANNEL_MAX;
    }

    @Override
    public void tick() {
        if (this.age < MAX_AGE) {
            this.age++;
        } else {
            this.expired = true;
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public float alpha(float partialTick) {
        return 1.0F - (this.age + partialTick) / MAX_AGE;
    }

    private static List<Vec3> trace(Trajectory trajectory, Vec3 span, RandomSource random) {
        List<Vec3> points = new ArrayList<>(MAX_SAMPLES + 2);
        points.add(Vec3.ZERO);
        double reach = trajectory.speed();
        for (int step = 1; step <= MAX_SAMPLES; step++) {
            if (trajectory.positionAt(step - 1).distanceTo(span) <= reach) {
                break;
            }
            points.add(trajectory.positionAt(step).add(scatter(random)));
        }
        points.add(span);
        return List.copyOf(points);
    }

    private static Vec3 scatter(RandomSource random) {
        return new Vec3(jitter(random), jitter(random), jitter(random));
    }

    private static double jitter(RandomSource random) {
        return JITTER * (random.nextDouble() - random.nextDouble());
    }

    private void crackleAlong(ClientLevel level, RandomSource random) {
        float[] tint = {brighten(this.colorR), brighten(this.colorG), brighten(this.colorB)};
        List<Vec3> interior = this.path.subList(1, this.path.size() - 1);
        for (Vec3 offset : interior) {
            this.spark(level, random, offset, tint);
        }
    }

    private void spark(ClientLevel level, RandomSource random, Vec3 offset, float... tint) {
        Vec3 at = this.origin.add(offset);
        int delay = random.nextInt(CRACKLE_DELAY_RANGE) + CRACKLE_DELAY_BASE;
        ClientEffects.sparkle(level, random, at.x, at.y, at.z, 0.0, 0.0, 0.0, tint[0], tint[1], tint[2], CRACKLE_SCALE, delay, CRACKLE_DECAY, CRACKLE_GRAVITY, CRACKLE_BASE_AGE);
    }

    private static float brighten(float value) {
        return Math.min(1.0F, value * CRACKLE_BRIGHTEN);
    }

    private record Trajectory(Vec3 velocity) {
        static Trajectory towards(Vec3 span, double hint) {
            double apex = hint + Math.max(0.0, span.y);
            double rise = Math.sqrt(apex * LAUNCH_GRAVITY);
            double run = Math.sqrt(span.x * span.x + span.z * span.z);
            if (run < VERTICAL_RUN_EPSILON) {
                return new Trajectory(new Vec3(0.0, rise, 0.0));
            }
            double slope = SLOPE_FACTOR * (apex + Math.sqrt(apex * (apex - span.y))) / run;
            double stride = rise / slope;
            return new Trajectory(new Vec3(span.x / run * stride, rise, span.z / run * stride));
        }

        double speed() {
            return this.velocity.length();
        }

        Vec3 positionAt(int step) {
            double drop = SAMPLE_DROP * step * (step - 1) / 2.0;
            return this.velocity.scale(step).subtract(0.0, drop, 0.0);
        }
    }
}
