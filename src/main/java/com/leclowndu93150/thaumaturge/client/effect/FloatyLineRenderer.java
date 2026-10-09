package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class FloatyLineRenderer {
    private static final Identifier WISPY_TEXTURE = TTIds.rl("textures/misc/wispy.png");

    private static final float CLOCK_SCALE_NUMERATOR = 50.0F;
    private static final float CLOCK_SCALE_DENOMINATOR = 30.0F;
    private static final double MIN_LINE_LENGTH = 0.1;
    private static final int MIN_SEGMENTS = 2;
    private static final int SEGMENTS_PER_ROUND_BLOCK = 2;
    private static final double MID_POINT = 0.5;
    private static final double TAPER_SLOPE = 2.0;
    private static final double SWAY_BLOCKS = 0.5;
    private static final double PHASE_LENGTH_FACTOR = 2.0;
    private static final double PHASE_CLOCK_DIVISOR = 5.0;
    private static final double[] AXIS_PERIODS = {4.0, 3.0, 2.0};
    private static final double TAIL_GLOW_START = 0.7;
    private static final double TAIL_GLOW_RANGE = 0.3;
    private static final double TAIL_GLOW_PEAK = 0.8;
    private static final float CHANNEL_SCALE = 255.0F;
    private static final int MAX_BLOCK_SKY_LIGHT = 15;
    private static final int FULL_BRIGHT = LightCoordsUtil.pack(MAX_BLOCK_SKY_LIGHT, MAX_BLOCK_SKY_LIGHT);
    private static final float EDGE_LOW_V = 1.0F;
    private static final float EDGE_HIGH_V = 0.0F;

    private FloatyLineRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 offset, float time, int color, float scrollSpeed, float fraction, float halfWidth) {
        collector.submitCustomGeometry(poseStack, renderType(), (pose, buffer) -> emit(pose.pose(), buffer, offset, time, color, scrollSpeed, fraction, halfWidth));
    }

    public static void draw(PoseStack poseStack, MultiBufferSource buffers, Vec3 offset, float time, int color, float scrollSpeed, float fraction, float halfWidth) {
        emit(poseStack.last().pose(), buffers.getBuffer(renderType()), offset, time, color, scrollSpeed, fraction, halfWidth);
    }

    public static float time(long gameTime, float partialTick) {
        return (gameTime + partialTick) * CLOCK_SCALE_NUMERATOR / CLOCK_SCALE_DENOMINATOR;
    }

    private static RenderType renderType() {
        return TTFXRenderTypes.additive(WISPY_TEXTURE);
    }

    private static void emit(Matrix4fc matrix, VertexConsumer buffer, Vec3 offset, float time, int color, float scrollSpeed, float fraction, float halfWidth) {
        double span = offset.length();
        if (span < MIN_LINE_LENGTH) {
            return;
        }
        int segments = Math.max(MIN_SEGMENTS, (int) Math.round(span) * SEGMENTS_PER_ROUND_BLOCK);
        int count = (int) (segments * fraction) + 1;
        RibbonSample[] samples = sampleCurve(offset, span, segments, count, time, scrollSpeed);
        float red = ARGB.red(color) / CHANNEL_SCALE;
        float green = ARGB.green(color) / CHANNEL_SCALE;
        float blue = ARGB.blue(color) / CHANNEL_SCALE;
        for (RibbonAxis axis : RibbonAxis.values()) {
            emitRibbon(buffer, matrix, samples, axis, halfWidth, red, green, blue);
        }
    }

    private static RibbonSample[] sampleCurve(Vec3 offset, double span, int segments, int count, float time, float scrollSpeed) {
        double[] reach = {offset.x, offset.y, offset.z};
        RibbonSample[] samples = new RibbonSample[count];
        for (int index = 0; index < count; index++) {
            double t = (double) index / segments;
            double taper = 1.0 - TAPER_SLOPE * Math.abs(t - MID_POINT);
            double sway = SWAY_BLOCKS * taper;
            double phase = span * (1.0 - t) * PHASE_LENGTH_FACTOR - time / PHASE_CLOCK_DIVISOR;
            double[] position = new double[AXIS_PERIODS.length];
            for (int axis = 0; axis < AXIS_PERIODS.length; axis++) {
                position[axis] = (reach[axis] + sway * Math.sin(phase / AXIS_PERIODS[axis])) * t;
            }
            double alpha = Math.max(taper, 0.0);
            if (t > TAIL_GLOW_START) {
                alpha = Math.max(alpha, (t - TAIL_GLOW_START) / TAIL_GLOW_RANGE * TAIL_GLOW_PEAK);
            }
            float u = (float) ((1.0 - t) * span - time * scrollSpeed);
            samples[index] = new RibbonSample(position[0], position[1], position[2], u, (float) alpha);
        }
        return samples;
    }

    private static void emitRibbon(VertexConsumer buffer, Matrix4fc matrix, RibbonSample[] samples, RibbonAxis axis, float halfWidth, float red, float green, float blue) {
        for (int index = 0; index < samples.length - 1; index++) {
            RibbonSample from = samples[index];
            RibbonSample to = samples[index + 1];
            writeVertex(buffer, matrix, from, axis, -halfWidth, EDGE_LOW_V, red, green, blue);
            writeVertex(buffer, matrix, from, axis, halfWidth, EDGE_HIGH_V, red, green, blue);
            writeVertex(buffer, matrix, to, axis, halfWidth, EDGE_HIGH_V, red, green, blue);
            writeVertex(buffer, matrix, to, axis, -halfWidth, EDGE_LOW_V, red, green, blue);
        }
    }

    private static void writeVertex(VertexConsumer buffer, Matrix4fc matrix, RibbonSample sample, RibbonAxis axis, float shift, float v, float red, float green, float blue) {
        buffer.addVertex(matrix, (float) (sample.x() + axis.stepX() * shift), (float) (sample.y() + axis.stepY() * shift), (float) sample.z()).setUv(sample.u(), v)
                .setColor(red, green, blue, sample.alpha()).setLight(FULL_BRIGHT);
    }

    private enum RibbonAxis {
        VERTICAL(0.0F, 1.0F), HORIZONTAL(1.0F, 0.0F);

        private final float stepX;
        private final float stepY;

        RibbonAxis(float stepX, float stepY) {
            this.stepX = stepX;
            this.stepY = stepY;
        }

        private float stepX() {
            return this.stepX;
        }

        private float stepY() {
            return this.stepY;
        }
    }

    private record RibbonSample(double x, double y, double z, float u, float alpha) {
    }
}
