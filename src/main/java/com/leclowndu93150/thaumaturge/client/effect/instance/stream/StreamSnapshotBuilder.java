package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.Snapshot;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.TrailPoint;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public final class StreamSnapshotBuilder {
    private static final int MIN_POINTS = 3;
    private static final int TAIL_TAPER_POINTS = 10;
    private static final int TAIL_TAPER_OFFSET = 12;
    private static final double TAIL_TAPER_SPAN = 10.0;
    private static final double QUARTER_TURN = Math.PI / 2.0;
    private static final int SLOT_SHOULDER = 2;
    private static final int SLOT_MID = 3;
    private static final int SLOT_FULL = 4;
    private static final double SHOULDER_BASE_FACTOR = 0.5;
    private static final double MID_BLEND_DIVISOR = 2.0;
    private static final double FULL_BLEND_DIVISOR = 3.0;
    private static final double RIPPLE_DEPTH = 0.2;
    private static final double RIPPLE_DIVISOR = 3.0;
    private static final double WOBBLE_DIVISOR_X = 6.0;
    private static final double WOBBLE_DIVISOR_Y = 7.0;
    private static final double WOBBLE_DIVISOR_Z = 8.0;
    private static final float TEX_SLICE = 0.075F;
    private static final int AXES = 3;
    private static final int CHANNELS = 4;

    private double[][] points = new double[0][];
    private float[][] colours = new float[0][];
    private double[] radii = new double[0];

    public @Nullable Snapshot build(TrailBuffer trail, StreamFrame frame, SnapshotRequest request, SlotColouring colouring) {
        int count = trail.size();
        if (count < MIN_POINTS) {
            return null;
        }
        fit(count);
        double wobble = request.wobble();
        for (int index = 0; index < count; index++) {
            TrailPoint point = trail.get(index);
            int behind = count - 1 - index;
            int wave = behind + frame.age();
            int slot = request.newestFirst() ? behind : index;
            double[] out = this.points[slot];
            out[0] = point.x() + wobble * Math.sin(wave / WOBBLE_DIVISOR_X);
            out[1] = point.y() + wobble * Math.sin(wave / WOBBLE_DIVISOR_Y);
            out[2] = point.z() + wobble * Math.sin(wave / WOBBLE_DIVISOR_Z);
            double radius = point.radius() * ripple(wave) * request.radiusMultiplier() * tailFactor(behind, count);
            this.radii[slot] = anchor(slot, radius, frame.baseRadius());
            colouring.colour(this.colours[slot], wave);
        }
        float start = trail.isDissolving() ? TEX_SLICE * (frame.age() - trail.dissolveStartAge() + request.partialTick()) : 0.0F;
        return new Snapshot(this.points, this.colours, this.radii, frame.originX(), frame.originY(), frame.originZ(), TEX_SLICE, Mth.frac(start));
    }

    private void fit(int count) {
        if (this.radii.length == count) {
            return;
        }
        this.points = new double[count][AXES];
        this.colours = new float[count][CHANNELS];
        this.radii = new double[count];
    }

    private static double tailFactor(int behind, int count) {
        if (behind <= count - TAIL_TAPER_POINTS) {
            return 1.0;
        }
        return Math.max(0.0, Math.cos((behind - (count - TAIL_TAPER_OFFSET)) / TAIL_TAPER_SPAN * QUARTER_TURN));
    }

    private static double anchor(int slot, double radius, double base) {
        if (slot < SLOT_SHOULDER) {
            return 0.0;
        }
        if (slot == SLOT_SHOULDER) {
            return (SHOULDER_BASE_FACTOR * base + radius) / MID_BLEND_DIVISOR;
        }
        if (slot == SLOT_MID) {
            return (base + radius) / MID_BLEND_DIVISOR;
        }
        if (slot == SLOT_FULL) {
            return (base + MID_BLEND_DIVISOR * radius) / FULL_BLEND_DIVISOR;
        }
        return radius;
    }

    private static double ripple(int wave) {
        return 1.0 + RIPPLE_DEPTH * Math.sin(wave / RIPPLE_DIVISOR);
    }
}
