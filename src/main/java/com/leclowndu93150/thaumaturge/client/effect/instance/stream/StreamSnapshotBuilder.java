package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.Snapshot;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.TrailPoint;
import org.jspecify.annotations.Nullable;

public final class StreamSnapshotBuilder {
    private static final int MIN_TRAIL_POINTS = 3;
    private static final int POSITION_AXES = 3;
    private static final int COLOUR_CHANNELS = 4;
    private static final double[] WOBBLE_DIVISORS = {6.0, 7.0, 8.0};
    private static final double RADIUS_RIPPLE_DIVISOR = 3.0;
    private static final double RADIUS_RIPPLE_DEPTH = 0.2;
    private static final int TAPER_WINDOW = 10;
    private static final int TAPER_ZERO_OFFSET = 12;
    private static final double TAPER_STEPS = 10.0;
    private static final double QUARTER_TURN = Math.PI / 2.0;
    private static final double THIRD = 1.0 / 3.0;
    private static final double[][] BLEND_WEIGHTS = {{0.0, 0.0}, {0.0, 0.0}, {0.25, 0.5}, {0.5, 0.5}, {THIRD, 2.0 * THIRD}};
    private static final double[] PLAIN_WEIGHTS = {0.0, 1.0};
    private static final float TEXTURE_SLICE = 0.075F;

    private StreamSnapshotBuilder() {}

    public static @Nullable Snapshot build(TrailBuffer trail, StreamFrame frame, SnapshotRequest request, SlotColouring colouring) {
        int size = trail.size();
        if (size < MIN_TRAIL_POINTS) {
            return null;
        }
        double[][] points = positions(trail, frame, request);
        double[] radii = radii(trail, frame, request);
        float[][] colours = colours(size, request, colouring);
        float start = trail.isDissolving() ? TEXTURE_SLICE * (frame.age() - trail.dissolveStartAge() + request.partialTick()) : 0.0F;
        return new Snapshot(points, colours, radii, frame.originX(), frame.originY(), frame.originZ(), TEXTURE_SLICE, start);
    }

    private static int phaseOf(int outputIndex, int size, boolean newestFirst) {
        return newestFirst ? outputIndex : size - 1 - outputIndex;
    }

    private static TrailPoint pointAt(TrailBuffer trail, int phase) {
        return trail.get(trail.size() - 1 - phase);
    }

    private static double[][] positions(TrailBuffer trail, StreamFrame frame, SnapshotRequest request) {
        int size = trail.size();
        double[][] points = new double[size][POSITION_AXES];
        for (int output = 0; output < size; output++) {
            int phase = phaseOf(output, size, request.newestFirst());
            TrailPoint point = pointAt(trail, phase);
            double wave = phase + frame.age();
            points[output][0] = point.x() + request.wobble() * Math.sin(wave / WOBBLE_DIVISORS[0]);
            points[output][1] = point.y() + request.wobble() * Math.sin(wave / WOBBLE_DIVISORS[1]);
            points[output][2] = point.z() + request.wobble() * Math.sin(wave / WOBBLE_DIVISORS[2]);
        }
        return points;
    }

    private static double[] radii(TrailBuffer trail, StreamFrame frame, SnapshotRequest request) {
        int size = trail.size();
        double[] radii = new double[size];
        for (int output = 0; output < size; output++) {
            int phase = phaseOf(output, size, request.newestFirst());
            double wave = phase + frame.age();
            double raw = pointAt(trail, phase).radius() * (1.0 + RADIUS_RIPPLE_DEPTH * Math.sin(wave / RADIUS_RIPPLE_DIVISOR)) * request.radiusMultiplier();
            if (phase > size - TAPER_WINDOW) {
                raw *= Math.cos((phase - (size - TAPER_ZERO_OFFSET)) / TAPER_STEPS * QUARTER_TURN);
            }
            double[] weights = output < BLEND_WEIGHTS.length ? BLEND_WEIGHTS[output] : PLAIN_WEIGHTS;
            radii[output] = weights[0] * frame.baseRadius() + weights[1] * raw;
        }
        return radii;
    }

    private static float[][] colours(int size, SnapshotRequest request, SlotColouring colouring) {
        float[][] colours = new float[size][COLOUR_CHANNELS];
        for (int output = 0; output < size; output++) {
            colouring.colour(colours[output], phaseOf(output, size, request.newestFirst()));
        }
        return colours;
    }
}
