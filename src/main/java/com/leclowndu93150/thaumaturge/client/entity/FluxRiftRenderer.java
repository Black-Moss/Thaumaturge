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
    private static final float SWAY_BLOCKS = 0.035F;
    private static final float SWAY_RATE_X = 0.05F;
    private static final float SWAY_RATE_Y = 0.043F;
    private static final float SWAY_RATE_Z = 0.061F;
    private static final float SWAY_STEP = 0.45F;
    private static final float THROB_RATE = 0.17F;
    private static final float THROB_STEP = 0.6F;
    private static final float THROB_DEPTH = 0.12F;
    private static final float INNER_HALO = 1.4F;
    private static final float MIDDLE_HALO = 1.75F;
    private static final float OUTER_HALO = 2.2F;
    private static final float CORE = 1.0F;
    private static final double VERTICAL_THRESHOLD = 0.9;
    private static final double TANGENT_EPSILON = 1.0E-8;

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
        float sway = SWAY_BLOCKS * motion;
        float throb = THROB_DEPTH * motion;
        for (int i = 0; i < count; i++) {
            Vec3 point = outline.get(i);
            int at = i * COMPONENTS;
            state.path[at] = (float) point.x + sway * Mth.sin(time * SWAY_RATE_X + i * SWAY_STEP);
            state.path[at + 1] = (float) point.y + sway * Mth.sin(time * SWAY_RATE_Y + i * SWAY_STEP);
            state.path[at + 2] = (float) point.z + sway * Mth.sin(time * SWAY_RATE_Z + i * SWAY_STEP);
            state.radii[i] = widths.get(i) * (1.0F + throb * Mth.sin(time * THROB_RATE + i * THROB_STEP));
        }
        buildRings(state.path, state.rings, count);
    }

    @Override
    public void submit(FluxRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.count < 2) {
            return;
        }
        submitLayer(state, poseStack, collector, TTFXRenderTypes.RIFT_GLOW, OUTER_HALO);
        submitLayer(state, poseStack, collector, TTFXRenderTypes.RIFT_GLOW, MIDDLE_HALO);
        submitLayer(state, poseStack, collector, state.goggles ? TTFXRenderTypes.RIFT_GLOW_NO_DEPTH : TTFXRenderTypes.RIFT_GLOW, INNER_HALO);
        submitLayer(state, poseStack, collector, TTFXRenderTypes.RIFT_CORE, CORE);
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
        double nx = 0.0;
        double ny = 0.0;
        double nz = 0.0;
        double tx = 0.0;
        double ty = 1.0;
        double tz = 0.0;
        for (int i = 0; i < count; i++) {
            int before = Math.max(0, i - 1) * COMPONENTS;
            int after = Math.min(count - 1, i + 1) * COMPONENTS;
            double dx = path[after] - path[before];
            double dy = path[after + 1] - path[before + 1];
            double dz = path[after + 2] - path[before + 2];
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length > TANGENT_EPSILON) {
                tx = dx / length;
                ty = dy / length;
                tz = dz / length;
            }
            if (i == 0) {
                boolean steep = Math.abs(ty) > VERTICAL_THRESHOLD;
                nx = steep ? 1.0 : 0.0;
                ny = steep ? 0.0 : 1.0;
                nz = 0.0;
            }
            double along = nx * tx + ny * ty + nz * tz;
            nx -= along * tx;
            ny -= along * ty;
            nz -= along * tz;
            double normal = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (normal < TANGENT_EPSILON) {
                boolean steep = Math.abs(ty) > VERTICAL_THRESHOLD;
                nx = steep ? 1.0 - tx * tx : -ty * tx;
                ny = steep ? -tx * ty : 1.0 - ty * ty;
                nz = steep ? -tx * tz : -ty * tz;
                normal = Math.sqrt(nx * nx + ny * ny + nz * nz);
            }
            nx /= normal;
            ny /= normal;
            nz /= normal;
            double bx = ty * nz - tz * ny;
            double by = tz * nx - tx * nz;
            double bz = tx * ny - ty * nx;
            for (int side = 0; side < SIDES; side++) {
                double angle = side * Mth.TWO_PI / SIDES;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                int at = i * RING_STRIDE + side * COMPONENTS;
                rings[at] = (float) (nx * cos + bx * sin);
                rings[at + 1] = (float) (ny * cos + by * sin);
                rings[at + 2] = (float) (nz * cos + bz * sin);
            }
        }
    }
}
