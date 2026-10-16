package com.leclowndu93150.thaumaturge.content.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RiftOutline {
    private static final int SIZE_PER_SEGMENT = 3;
    private static final int MAX_SEGMENTS = Mth.positiveCeilDiv(EntityFluxRift.MAX_RIFT_SIZE, SIZE_PER_SEGMENT);
    private static final double SEGMENT_LENGTH = 0.2;
    private static final double TIP_LENGTH = SEGMENT_LENGTH / 2.0;
    private static final float TURN_STANDARD_DEVIATION = 0.33F;
    private static final double SIZE_PER_RADIUS = 300.0;
    private static final double MIN_HALF_EXTENT = 0.05;

    private final List<Vec3> points = new ArrayList<>();
    private final List<Float> widths = new ArrayList<>();
    private final List<Vec3> pointView = Collections.unmodifiableList(this.points);
    private final List<Float> widthView = Collections.unmodifiableList(this.widths);
    private final Vec3[] outward = new Vec3[MAX_SEGMENTS + 1];
    private final Vec3[] inward = new Vec3[MAX_SEGMENTS + 1];
    private final Vec3[] outwardTips = new Vec3[MAX_SEGMENTS + 1];
    private final Vec3[] inwardTips = new Vec3[MAX_SEGMENTS + 1];

    private Vec3 startHeading = Vec3.ZERO;
    private boolean grown;
    private int grownSeed;
    private AABB bounds = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(MIN_HALF_EXTENT);

    List<Vec3> points() {
        return this.pointView;
    }

    List<Float> widths() {
        return this.widthView;
    }

    AABB bounds() {
        return this.bounds;
    }

    void shape(int seed, int size) {
        if (!this.grown || this.grownSeed != seed) {
            growArms(seed);
        }
        assemble(Mth.clamp(size, 0, EntityFluxRift.MAX_RIFT_SIZE));
    }

    static int segmentsFor(int size) {
        return size <= 0 ? 0 : Math.min(MAX_SEGMENTS, Mth.positiveCeilDiv(size, SIZE_PER_SEGMENT));
    }

    private void growArms(int seed) {
        RandomSource random = RandomSource.create(seed);
        this.startHeading = randomDirection(random);
        Vec3 rightHeading = this.startHeading;
        Vec3 leftHeading = this.startHeading.reverse();
        this.outward[0] = Vec3.ZERO;
        this.inward[0] = Vec3.ZERO;
        for (int step = 1; step <= MAX_SEGMENTS; step++) {
            rightHeading = turn(random, rightHeading);
            place(this.outward, this.outwardTips, step, rightHeading);
            leftHeading = turn(random, leftHeading);
            place(this.inward, this.inwardTips, step, leftHeading);
        }
        this.grown = true;
        this.grownSeed = seed;
    }

    private static Vec3 randomDirection(RandomSource random) {
        double y = random.nextDouble() * 2.0 - 1.0;
        double angle = random.nextDouble() * Mth.TWO_PI;
        double ring = Math.sqrt(1.0 - y * y);
        return new Vec3(Math.cos(angle) * ring, y, Math.sin(angle) * ring);
    }

    private static void place(Vec3[] joints, Vec3[] tips, int step, Vec3 heading) {
        joints[step] = joints[step - 1].add(heading.scale(SEGMENT_LENGTH));
        tips[step] = joints[step].add(heading.scale(TIP_LENGTH));
    }

    private static Vec3 turn(RandomSource random, Vec3 heading) {
        float aboutX = (float) random.nextGaussian() * TURN_STANDARD_DEVIATION;
        float aboutY = (float) random.nextGaussian() * TURN_STANDARD_DEVIATION;
        return heading.xRot(aboutX).yRot(aboutY);
    }

    private void assemble(int size) {
        this.points.clear();
        this.widths.clear();
        int segments = segmentsFor(size);
        if (segments == 0) {
            this.points.add(this.startHeading.reverse().scale(TIP_LENGTH));
            this.widths.add(0.0F);
            this.points.add(this.startHeading.scale(TIP_LENGTH));
            this.widths.add(0.0F);
            this.bounds = enclose();
            return;
        }
        double radiusScale = size / SIZE_PER_RADIUS;
        this.points.add(this.inwardTips[segments]);
        this.widths.add(0.0F);
        for (int ring = segments; ring >= 1; ring--) {
            addJoint(this.inward[ring], ring, segments, radiusScale);
        }
        for (int ring = 1; ring <= segments; ring++) {
            addJoint(this.outward[ring], ring, segments, radiusScale);
        }
        this.points.add(this.outwardTips[segments]);
        this.widths.add(0.0F);
        this.bounds = enclose();
    }

    private void addJoint(Vec3 joint, int ring, int segments, double radiusScale) {
        this.points.add(joint);
        this.widths.add((float) Math.max(0.0, radiusScale * (1.0 - (double) ring / segments)));
    }

    private AABB enclose() {
        double minX = 0.0;
        double minY = 0.0;
        double minZ = 0.0;
        double maxX = 0.0;
        double maxY = 0.0;
        double maxZ = 0.0;
        for (Vec3 point : this.points) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            minZ = Math.min(minZ, point.z);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            maxZ = Math.max(maxZ, point.z);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
