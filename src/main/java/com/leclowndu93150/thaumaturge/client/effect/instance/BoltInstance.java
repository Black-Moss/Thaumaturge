package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BoltInstance implements IFXInstance {
    static final int MAX_AGE = 3;

    private static final float CHANNEL_MAX = 255.0F;
    private static final int PHASE_STEPS = 50;
    private static final int SEED_RANGE = 1000;
    private static final int MIN_POINTS = 3;
    private static final int MAX_POINTS = 512;
    private static final float AMPLITUDE_PER_TICK = 0.1F;
    private static final double PERIOD_X = 4.0;
    private static final double PERIOD_Y = 3.0;
    private static final double PERIOD_Z = 2.0;
    private static final double JITTER = 0.1;
    private static final int THIN_ONE_IN = 4;
    private static final float THIN_STEP = 0.25F;
    private static final float MIN_ALPHA = 0.1F;

    public final double startX;
    public final double startY;
    public final double startZ;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float width;
    public final float length;

    private final Vec3 span;
    private final List<BoltNode> nodes;
    private int age;
    private boolean expired;

    public BoltInstance(double startX, double startY, double startZ, double targetX, double targetY, double targetZ, int color, float width) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.colorR = ARGB.red(color) / CHANNEL_MAX;
        this.colorG = ARGB.green(color) / CHANNEL_MAX;
        this.colorB = ARGB.blue(color) / CHANNEL_MAX;
        this.width = width;
        this.span = new Vec3(targetX - startX, targetY - startY, targetZ - startZ);
        this.length = (float) (this.span.length() * Math.PI);
        RandomSource random = StreamInstance.currentRandom();
        double phaseOffset = random.nextInt(PHASE_STEPS) * Math.PI;
        int seed = random.nextInt(SEED_RANGE);
        this.nodes = Float.isFinite(this.length) ? buildNodes(this.span, this.length, phaseOffset, RandomSource.create(seed)) : List.of();
    }

    @Override
    public void tick() {
        if (this.age >= MAX_AGE) {
            this.expired = true;
        } else {
            this.age++;
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public List<PathStep> computePath(float partialTick) {
        if (this.nodes.isEmpty()) {
            return List.of();
        }
        List<PathStep> path = new ArrayList<>(this.nodes.size() + 2);
        double amplitude = (this.age + partialTick) * AMPLITUDE_PER_TICK;
        float thinWidth = (1.0F - this.age * THIN_STEP) * this.width;
        path.add(new PathStep(0.0, 0.0, 0.0, this.width));
        for (BoltNode node : this.nodes) {
            double x = node.baseX() + amplitude * Math.sin(node.phase() / PERIOD_X) + node.jitterX();
            double y = node.baseY() + amplitude * Math.sin(node.phase() / PERIOD_Y) + node.jitterY();
            double z = node.baseZ() + amplitude * Math.sin(node.phase() / PERIOD_Z) + node.jitterZ();
            path.add(new PathStep(x, y, z, node.thin() ? thinWidth : this.width));
        }
        path.add(new PathStep(this.span.x, this.span.y, this.span.z, this.width));
        return path;
    }

    public float alpha(float partialTick) {
        return Mth.clamp(1.0F - (this.age + partialTick) / MAX_AGE, MIN_ALPHA, 1.0F);
    }

    private static double jitter(RandomSource random) {
        return JITTER * (random.nextDouble() - random.nextDouble());
    }

    private static List<BoltNode> buildNodes(Vec3 span, float length, double phaseOffset, RandomSource random) {
        int steps = Mth.clamp((int) length, MIN_POINTS, MAX_POINTS);
        float stride = length / steps;
        List<BoltNode> nodes = new ArrayList<>(steps);
        for (int i = 1; i < steps - 1; i++) {
            double fraction = (double) i / steps;
            double phase = i * stride + phaseOffset;
            double jitterX = jitter(random);
            double jitterY = jitter(random);
            double jitterZ = jitter(random);
            boolean thin = random.nextInt(THIN_ONE_IN) == 0;
            nodes.add(new BoltNode(span.x * fraction, span.y * fraction, span.z * fraction, phase, jitterX, jitterY, jitterZ, thin));
        }
        return List.copyOf(nodes);
    }

    public record PathStep(double x, double y, double z, float width) {
    }

    private record BoltNode(double baseX, double baseY, double baseZ, double phase, double jitterX, double jitterY, double jitterZ, boolean thin) {
    }
}
