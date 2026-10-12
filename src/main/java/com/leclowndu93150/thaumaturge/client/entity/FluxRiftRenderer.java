package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class FluxRiftRenderer extends EntityRenderer<EntityFluxRift, FluxRiftRenderState> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final int SIDES = 6;
    private static final int COMPONENTS = 3;
    private static final int RING_STRIDE = SIDES * COMPONENTS;
    private static final float STILL_STABILITY = 50.0F;
    private static final float MAX_MOTION = 1.5F;
    private static final float WOBBLE_BLOCKS = 0.1F;
    private static final float WOBBLE_DIVISOR_X = 50.0F;
    private static final float WOBBLE_DIVISOR_Y = 60.0F;
    private static final float WOBBLE_DIVISOR_Z = 70.0F;
    private static final float POINT_PHASE_TICKS = 10.0F;
    private static final float PULSE_DEPTH = 0.1F;
    private static final float PULSE_DIVISOR = 8.0F;
    private static final float GOGGLE_HALO = 1.25F;
    private static final float MIDDLE_HALO = 1.75F;
    private static final float OUTER_HALO = 2.25F;
    private static final float CORE = 1.0F;
    private static final int MIN_DRAWN_POINTS = 3;
    private static final double VERTICAL_THRESHOLD = 0.99;
    private static final double TANGENT_EPSILON = 1.0E-8;
    private static final double SIDE_RADIANS = Math.PI / 3.0;

    public FluxRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public FluxRiftRenderState createRenderState() {
        return new FluxRiftRenderState();
    }

    @Override
    public void extractRenderState(EntityFluxRift entity, FluxRiftRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.goggles = GogglesAccess.wearsRevealingGear(Minecraft.getInstance().player);
        List<Vec3> outline = entity.outline;
        List<Float> widths = entity.outlineWidths;
        int count = Math.min(outline.size(), widths.size());
        state.count = count;
        if (state.path.length < count * COMPONENTS) {
            state.path = new float[count * COMPONENTS];
            state.radii = new float[count];
            state.rings = new float[count * RING_STRIDE];
        }
        float motion = Mth.clamp(1.0F - entity.stabilityValue() / STILL_STABILITY, 0.0F, MAX_MOTION);
        float time = state.ageInTicks;
        float wobble = WOBBLE_BLOCKS * motion;
        float pulse = PULSE_DEPTH * motion;
        int middle = count / 2;
        for (int i = 0; i < count; i++) {
            Vec3 point = outline.get(i);
            int at = i * COMPONENTS;
            float phase = i < middle ? time + POINT_PHASE_TICKS * i : i == middle ? time : time - POINT_PHASE_TICKS * i;
            state.path[at] = (float) point.x + wobble * Mth.sin(phase / WOBBLE_DIVISOR_X);
            state.path[at + 1] = (float) point.y + wobble * Mth.sin(phase / WOBBLE_DIVISOR_Y);
            state.path[at + 2] = (float) point.z + wobble * Mth.sin(phase / WOBBLE_DIVISOR_Z);
            state.radii[i] = widths.get(i) * (1.0F - pulse * Mth.sin(phase / PULSE_DIVISOR));
        }
        buildRings(state.path, state.rings, count);
    }

    @Override
    public void submit(FluxRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.count < MIN_DRAWN_POINTS) {
            return;
        }
        submitLayer(state, poseStack, collector, state.goggles ? TTFXRenderTypes.RIFT_GLOW_NO_DEPTH : TTFXRenderTypes.RIFT_GLOW, GOGGLE_HALO);
        submitLayer(state, poseStack, collector, TTFXRenderTypes.RIFT_GLOW, MIDDLE_HALO);
        submitLayer(state, poseStack, collector, TTFXRenderTypes.RIFT_GLOW, OUTER_HALO);
        submitLayer(state, poseStack, collector, TTFXRenderTypes.RIFT_SOLID, CORE);
    }

    private static void submitLayer(FluxRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, RenderType type, float scale) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> writeTube(buffer, pose.pose(), state, scale));
    }

    private static void writeTube(VertexConsumer buffer, Matrix4fc matrix, FluxRiftRenderState state, float scale) {
        for (int i = 0; i < state.count - 1; i++) {
            for (int side = 0; side < SIDES; side++) {
                int next = (side + 1) % SIDES;
                ringVertex(buffer, matrix, state, scale, i, side);
                ringVertex(buffer, matrix, state, scale, i + 1, side);
                ringVertex(buffer, matrix, state, scale, i + 1, next);
                ringVertex(buffer, matrix, state, scale, i, next);
            }
        }
    }

    private static void ringVertex(VertexConsumer buffer, Matrix4fc matrix, FluxRiftRenderState state, float scale, int index, int side) {
        int point = index * COMPONENTS;
        int ring = index * RING_STRIDE + side * COMPONENTS;
        float radius = state.radii[index] * scale;
        buffer.addVertex(matrix, state.path[point] + state.rings[ring] * radius, state.path[point + 1] + state.rings[ring + 1] * radius, state.path[point + 2] + state.rings[ring + 2] * radius);
    }

    private static void buildRings(float[] path, float[] rings, int count) {
        for (int i = 0; i < count; i++) {
            int before = Math.max(0, i - 1) * COMPONENTS;
            int after = Math.min(count - 1, i + 1) * COMPONENTS;
            double dx = path[after] - path[before];
            double dy = path[after + 1] - path[before + 1];
            double dz = path[after + 2] - path[before + 2];
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double tx = 0.0;
            double ty = 1.0;
            double tz = 0.0;
            if (length > TANGENT_EPSILON) {
                tx = dx / length;
                ty = dy / length;
                tz = dz / length;
            }
            boolean steep = Math.abs(ty) >= VERTICAL_THRESHOLD;
            double rx = steep ? 1.0 : 0.0;
            double ry = steep ? 0.0 : 1.0;
            double ax = -tz * ry;
            double ay = tz * rx;
            double az = tx * ry - ty * rx;
            double aLength = Math.sqrt(ax * ax + ay * ay + az * az);
            ax /= aLength;
            ay /= aLength;
            az /= aLength;
            double bx = ty * az - tz * ay;
            double by = tz * ax - tx * az;
            double bz = tx * ay - ty * ax;
            double bLength = Math.sqrt(bx * bx + by * by + bz * bz);
            bx /= bLength;
            by /= bLength;
            bz /= bLength;
            for (int side = 0; side < SIDES; side++) {
                double angle = side * SIDE_RADIANS;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                int at = i * RING_STRIDE + side * COMPONENTS;
                rings[at] = (float) (ax * cos + bx * sin);
                rings[at + 1] = (float) (ay * cos + by * sin);
                rings[at + 2] = (float) (az * cos + bz * sin);
            }
        }
    }
}
