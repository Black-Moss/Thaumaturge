package com.leclowndu93150.thaumaturge.client.effect.instance.stream;

import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.Snapshot;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance.TrailPoint;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public final class StreamSnapshotBuilder {
    private static final int MIN_POINTS = 3;
    private static final int PINNED_TAIL_POINTS = 2;
    private static final int TAIL_BLEND_POINTS = 4;
    private static final int HEAD_TAPER_POINTS = 5;
    private static final double QUARTER_TURN = Math.PI / 2.0;
    private static final double RIPPLE_DEPTH = 0.12;
    private static final double RIPPLE_SPACING = 0.6;
    private static final double RIPPLE_SPEED = 0.25;
    private static final double SHIMMER_RATE_X = 0.11;
    private static final double SHIMMER_RATE_Y = 0.087;
    private static final double SHIMMER_RATE_Z = 0.13;
    private static final double SHIMMER_SPACING_X = 0.45;
    private static final double SHIMMER_SPACING_Y = 0.37;
    private static final double SHIMMER_SPACING_Z = 0.52;
    private static final double ORIGIN_PHASE = 1.7;
    private static final float TEX_SLICE = 0.08F;
    private static final float FLOW_SLICES_PER_TICK = 1.5F;
    private static final int SERIAL_BASE = PINNED_TAIL_POINTS - 1;
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
        boolean dissolving = trail.isDissolving();
        int newestSerial = SERIAL_BASE + (dissolving ? trail.dissolveStartAge() : frame.age());
        int oldestSerial = newestSerial - (count - 1);
        float time = frame.age() + request.partialTick();
        double phase = (frame.originX() + frame.originY() + frame.originZ()) * ORIGIN_PHASE;
        double wobble = request.wobble();
        for (int index = 0; index < count; index++) {
            TrailPoint point = trail.get(index);
            int serial = oldestSerial + index;
            int slot = request.newestFirst() ? count - 1 - index : index;
            double[] out = this.points[slot];
            out[0] = point.x() + wobble * Math.sin(time * SHIMMER_RATE_X + serial * SHIMMER_SPACING_X + phase);
            out[1] = point.y() + wobble * Math.sin(time * SHIMMER_RATE_Y + serial * SHIMMER_SPACING_Y + phase);
            out[2] = point.z() + wobble * Math.sin(time * SHIMMER_RATE_Z + serial * SHIMMER_SPACING_Z + phase);
            this.radii[slot] = point.radius() * taper(index, count) * ripple(serial, time) * request.radiusMultiplier();
            colouring.colour(this.colours[slot], serial);
        }
        float flow = dissolving ? (frame.age() - trail.dissolveStartAge() + request.partialTick()) * FLOW_SLICES_PER_TICK * TEX_SLICE : 0.0F;
        float start = request.newestFirst() ? flow - newestSerial * TEX_SLICE : oldestSerial * TEX_SLICE - flow;
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

    private static double taper(int index, int count) {
        if (index < PINNED_TAIL_POINTS) {
            return 0.0;
        }
        double factor = 1.0;
        int fromTail = index - PINNED_TAIL_POINTS + 1;
        if (fromTail <= TAIL_BLEND_POINTS) {
            factor = Mth.smoothstep(fromTail / (double) (TAIL_BLEND_POINTS + 1));
        }
        int fromHead = count - 1 - index;
        if (fromHead < HEAD_TAPER_POINTS) {
            factor = Math.min(factor, Math.sin(fromHead / (double) HEAD_TAPER_POINTS * QUARTER_TURN));
        }
        return factor;
    }

    private static double ripple(int serial, float time) {
        return 1.0 + RIPPLE_DEPTH * Math.sin(serial * RIPPLE_SPACING - time * RIPPLE_SPEED);
    }
}
