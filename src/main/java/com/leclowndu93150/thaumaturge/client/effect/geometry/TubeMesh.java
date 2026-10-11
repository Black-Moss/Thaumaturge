package com.leclowndu93150.thaumaturge.client.effect.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class TubeMesh {
    public static final int ROUND_SIDES = 12;

    private static final int MIN_POINTS = 3;
    private static final int MIN_SIDES = 3;
    private static final int AXES = 3;
    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;
    private static final int ALPHA = 3;
    private static final double SAME_POINT_DISTANCE_SQR = 1.0E-10;
    private static final double REVERSAL_LIMIT = -0.9999;
    private static final double PARALLEL_LIMIT = 0.99;
    private static final double MIN_PLANE_DOT = 1.0E-3;

    private TubeMesh() {}

    public static void draw(PoseStack poseStack, VertexConsumer consumer, double[][] points, float[][] colours, double[] radii, int light, float texSlice, float start) {
        draw(poseStack, consumer, points, colours, radii, light, texSlice, start, ROUND_SIDES);
    }

    public static void draw(PoseStack poseStack, VertexConsumer consumer, double[][] points, float[][] colours, double[] radii, int light, float texSlice, float start, int sides) {
        int total = Math.min(points.length, Math.min(colours.length, radii.length));
        if (total < MIN_POINTS) {
            return;
        }
        int[] kept = new int[total];
        int count = dedupe(points, total, kept);
        if (count < MIN_POINTS) {
            return;
        }
        int faces = Math.max(MIN_SIDES, sides);
        double[] cosines = new double[faces + 1];
        double[] sines = new double[faces + 1];
        for (int k = 0; k <= faces; k++) {
            double angle = Mth.TWO_PI * k / faces;
            cosines[k] = Math.cos(angle);
            sines[k] = Math.sin(angle);
        }
        Matrix4f matrix = poseStack.last().pose();
        double[] frame = new double[AXES * 3];
        double[] incoming = new double[AXES];
        double[] outgoing = new double[AXES];
        double[] previousRing = new double[(faces + 1) * AXES];
        double[] currentRing = new double[(faces + 1) * AXES];

        direction(points[kept[0]], points[kept[1]], incoming);
        direction(points[kept[1]], points[kept[2]], outgoing);
        seedFrame(outgoing, frame);
        ring(points[kept[1]], radii[kept[1]], incoming, outgoing, frame, cosines, sines, faces, previousRing);

        for (int joint = 2; joint < count - 1; joint++) {
            copy(outgoing, incoming);
            direction(points[kept[joint]], points[kept[joint + 1]], outgoing);
            transport(frame, incoming, outgoing);
            ring(points[kept[joint]], radii[kept[joint]], incoming, outgoing, frame, cosines, sines, faces, currentRing);
            float v0 = start + kept[joint - 1] * texSlice;
            float v1 = start + kept[joint] * texSlice;
            emitSection(consumer, matrix, previousRing, currentRing, colours[kept[joint - 1]], colours[kept[joint]], faces, v0, v1, light);
            double[] swap = previousRing;
            previousRing = currentRing;
            currentRing = swap;
        }
    }

    private static int dedupe(double[][] points, int total, int[] kept) {
        int count = 0;
        for (int i = 0; i < total; i++) {
            if (count > 0 && distanceSqr(points[kept[count - 1]], points[i]) < SAME_POINT_DISTANCE_SQR) {
                continue;
            }
            kept[count++] = i;
        }
        return count;
    }

    private static double distanceSqr(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        return dx * dx + dy * dy + dz * dz;
    }

    private static void direction(double[] from, double[] to, double[] out) {
        double dx = to[0] - from[0];
        double dy = to[1] - from[1];
        double dz = to[2] - from[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        out[0] = dx / length;
        out[1] = dy / length;
        out[2] = dz / length;
    }

    private static void copy(double[] from, double[] to) {
        to[0] = from[0];
        to[1] = from[1];
        to[2] = from[2];
    }

    private static void seedFrame(double[] axis, double[] frame) {
        double hx = 0.0;
        double hy = 1.0;
        double hz = 0.0;
        if (Math.abs(axis[1]) > PARALLEL_LIMIT) {
            hx = 1.0;
            hy = 0.0;
        }
        double along = hx * axis[0] + hy * axis[1] + hz * axis[2];
        frame[0] = hx - along * axis[0];
        frame[1] = hy - along * axis[1];
        frame[2] = hz - along * axis[2];
        normalise(frame, 0);
        completeFrame(axis, frame);
    }

    private static void transport(double[] frame, double[] from, double[] to) {
        double cos = from[0] * to[0] + from[1] * to[1] + from[2] * to[2];
        double ux = frame[0];
        double uy = frame[1];
        double uz = frame[2];
        if (cos > REVERSAL_LIMIT) {
            double wx = from[1] * to[2] - from[2] * to[1];
            double wy = from[2] * to[0] - from[0] * to[2];
            double wz = from[0] * to[1] - from[1] * to[0];
            double wDotU = wx * ux + wy * uy + wz * uz;
            double scale = wDotU / (1.0 + cos);
            frame[0] = cos * ux + (wy * uz - wz * uy) + scale * wx;
            frame[1] = cos * uy + (wz * ux - wx * uz) + scale * wy;
            frame[2] = cos * uz + (wx * uy - wy * ux) + scale * wz;
        }
        double along = frame[0] * to[0] + frame[1] * to[1] + frame[2] * to[2];
        frame[0] -= along * to[0];
        frame[1] -= along * to[1];
        frame[2] -= along * to[2];
        if (!normalise(frame, 0)) {
            seedFrame(to, frame);
            return;
        }
        completeFrame(to, frame);
    }

    private static void completeFrame(double[] axis, double[] frame) {
        frame[3] = axis[1] * frame[2] - axis[2] * frame[1];
        frame[4] = axis[2] * frame[0] - axis[0] * frame[2];
        frame[5] = axis[0] * frame[1] - axis[1] * frame[0];
        frame[6] = axis[0];
        frame[7] = axis[1];
        frame[8] = axis[2];
    }

    private static boolean normalise(double[] values, int offset) {
        double length = Math.sqrt(values[offset] * values[offset] + values[offset + 1] * values[offset + 1] + values[offset + 2] * values[offset + 2]);
        if (length < MIN_PLANE_DOT) {
            return false;
        }
        values[offset] /= length;
        values[offset + 1] /= length;
        values[offset + 2] /= length;
        return true;
    }

    private static void ring(double[] centre, double radius, double[] incoming, double[] outgoing, double[] frame, double[] cosines, double[] sines, int faces, double[] out) {
        double nx = incoming[0] + outgoing[0];
        double ny = incoming[1] + outgoing[1];
        double nz = incoming[2] + outgoing[2];
        double planeDot = nx * outgoing[0] + ny * outgoing[1] + nz * outgoing[2];
        if (planeDot < MIN_PLANE_DOT) {
            nx = outgoing[0];
            ny = outgoing[1];
            nz = outgoing[2];
            planeDot = 1.0;
        }
        for (int k = 0; k <= faces; k++) {
            double ox = radius * (cosines[k] * frame[0] + sines[k] * frame[3]);
            double oy = radius * (cosines[k] * frame[1] + sines[k] * frame[4]);
            double oz = radius * (cosines[k] * frame[2] + sines[k] * frame[5]);
            double slide = -(ox * nx + oy * ny + oz * nz) / planeDot;
            int base = k * AXES;
            out[base] = centre[0] + ox + slide * outgoing[0];
            out[base + 1] = centre[1] + oy + slide * outgoing[1];
            out[base + 2] = centre[2] + oz + slide * outgoing[2];
        }
    }

    private static void emitSection(VertexConsumer consumer, Matrix4f matrix, double[] near, double[] far, float[] nearColour, float[] farColour, int faces, float v0, float v1, int light) {
        float nr = channel(nearColour, RED);
        float ng = channel(nearColour, GREEN);
        float nb = channel(nearColour, BLUE);
        float na = channel(nearColour, ALPHA);
        float fr = channel(farColour, RED);
        float fg = channel(farColour, GREEN);
        float fb = channel(farColour, BLUE);
        float fa = channel(farColour, ALPHA);
        for (int k = 0; k < faces; k++) {
            float u0 = (float) k / faces;
            float u1 = (float) (k + 1) / faces;
            int a = k * AXES;
            int b = (k + 1) * AXES;
            vertex(consumer, matrix, near, a, u0, v0, nr, ng, nb, na, light);
            vertex(consumer, matrix, near, b, u1, v0, nr, ng, nb, na, light);
            vertex(consumer, matrix, far, b, u1, v1, fr, fg, fb, fa, light);
            vertex(consumer, matrix, far, a, u0, v1, fr, fg, fb, fa, light);
        }
    }

    private static float channel(float[] colour, int index) {
        return index < colour.length ? Mth.clamp(colour[index], 0.0F, 1.0F) : 1.0F;
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double[] ring, int offset, float u, float v, float red, float green, float blue, float alpha, int light) {
        consumer.addVertex(matrix, (float) ring[offset], (float) ring[offset + 1], (float) ring[offset + 2]).setUv(u, v).setColor(red, green, blue, alpha).setLight(light);
    }
}
