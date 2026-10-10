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
    private static final double TURN_JITTER = 0.22;
    private static final float RADIUS_PER_SIZE = 0.0012F;
    private static final double MIN_HALF_EXTENT = 0.05;

    private final List<Vec3> points = new ArrayList<>();
    private final List<Float> widths = new ArrayList<>();
    private final List<Vec3> pointView = Collections.unmodifiableList(this.points);
    private final List<Float> widthView = Collections.unmodifiableList(this.widths);
    private final Vec3[] outward = new Vec3[MAX_SEGMENTS + 1];
    private final Vec3[] inward = new Vec3[MAX_SEGMENTS + 1];
    private final Vec3[] outwardTips = new Vec3[MAX_SEGMENTS + 1];
    private final Vec3[] inwardTips = new Vec3[MAX_SEGMENTS + 1];

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
        Vec3 heading = randomDirection(random);
        walk(random, heading, this.outward, this.outwardTips);
        walk(random, heading.reverse(), this.inward, this.inwardTips);
        this.grown = true;
        this.grownSeed = seed;
    }

    private static Vec3 randomDirection(RandomSource random) {
        double y = random.nextDouble() * 2.0 - 1.0;
        double angle = random.nextDouble() * Mth.TWO_PI;
        double ring = Math.sqrt(1.0 - y * y);
        return new Vec3(Math.cos(angle) * ring, y, Math.sin(angle) * ring);
    }

    private static void walk(RandomSource random, Vec3 start, Vec3[] joints, Vec3[] tips) {
        Vec3 heading = start;
        joints[0] = Vec3.ZERO;
        tips[0] = Vec3.ZERO;
        for (int step = 1; step <= MAX_SEGMENTS; step++) {
            joints[step] = joints[step - 1].add(heading.scale(SEGMENT_LENGTH));
            heading = turn(random, heading);
            tips[step] = joints[step].add(heading.scale(TIP_LENGTH));
        }
    }

    private static Vec3 turn(RandomSource random, Vec3 heading) {
        Vec3 nudged = heading.add(random.nextGaussian() * TURN_JITTER, random.nextGaussian() * TURN_JITTER, random.nextGaussian() * TURN_JITTER);
        return nudged.lengthSqr() < Mth.EPSILON ? heading : nudged.normalize();
    }

    private void assemble(int size) {
        this.points.clear();
        this.widths.clear();
        int segments = segmentsFor(size);
        float centreRadius = size * RADIUS_PER_SIZE;
        if (segments == 0) {
            this.points.add(Vec3.ZERO);
            this.widths.add(centreRadius);
            this.bounds = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(MIN_HALF_EXTENT);
            return;
        }
        double armLength = segments * SEGMENT_LENGTH + TIP_LENGTH;
        this.points.add(this.inwardTips[segments]);
        this.widths.add(0.0F);
        for (int step = segments; step >= 1; step--) {
            addJoint(this.inward[step], step, armLength, centreRadius);
        }
        addJoint(Vec3.ZERO, 0, armLength, centreRadius);
        for (int step = 1; step <= segments; step++) {
            addJoint(this.outward[step], step, armLength, centreRadius);
        }
        this.points.add(this.outwardTips[segments]);
        this.widths.add(0.0F);
        this.bounds = enclose();
    }

    private void addJoint(Vec3 joint, int step, double armLength, float centreRadius) {
        this.points.add(joint);
        this.widths.add((float) (centreRadius * (1.0 - step * SEGMENT_LENGTH / armLength)));
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
