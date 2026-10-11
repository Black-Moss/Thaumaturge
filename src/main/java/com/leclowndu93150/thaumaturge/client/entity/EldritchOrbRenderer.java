package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

public final class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb, EldritchOrbRenderer.State> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float HALF = 0.5F;
    private static final float CORE_HALF = 0.375F;
    private static final float CORE_FLICKER_BASE = 0.8F;
    private static final float CORE_FLICKER_DEPTH = 0.2F;
    private static final float CORE_FLICKER_RATE = 1.7F;
    private static final int RAYS = 12;
    private static final float REACH = 0.9F;
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_TICKS_PER_TURN = 90.0F;
    private static final float FULL_TURN = 360.0F;
    private static final int RAY_CORE = ARGB.color(235, 255, 255, 255);
    private static final int RAY_TIP = ARGB.color(110, 150, 148, 160);

    private static final int MAX_RAYS = 16;
    private static final int TIP_CORNERS = 3;
    private static final int COMPONENTS = 3;
    private static final int RAY_STRIDE = TIP_CORNERS * COMPONENTS;
    private static final float SHORTEST_RAY = 0.2F;
    private static final float THINNEST_RAY = 0.035F;
    private static final float WIDTH_SPREAD = 0.05F;
    private static final double SPREAD_STEP_U = 0.7548776662466927;
    private static final double SPREAD_STEP_V = 0.5698402909980532;
    private static final double LENGTH_STEP = 0.6180339887498949;
    private static final double WIDTH_STEP = 0.41421356237309515;
    private static final double LENGTH_SEED = 0.31;
    private static final double WIDTH_SEED = 0.17;
    private static final double VERTICAL_THRESHOLD = 0.9;
    private static final float BURST_TILT = 23.0F;
    private static final float[] RAY_TIPS = buildRayTips();

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.centre = entity.getBbHeight() * HALF;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float age = state.ageInTicks;
        int frame = Math.floorMod((int) age, ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        int tint = ARGB.white(Mth.clamp(CORE_FLICKER_BASE + CORE_FLICKER_DEPTH * Mth.sin(age * CORE_FLICKER_RATE), 0.0F, 1.0F));
        poseStack.pushPose();
        poseStack.translate(0.0F, state.centre, 0.0F);
        float grow = Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
        submitBurst(poseStack, collector, RAYS, REACH * grow, age / SPIN_TICKS_PER_TURN * FULL_TURN, RAY_CORE, RAY_TIP);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.ELDRITCH_ORB), (pose, buffer) -> writeCore(buffer, pose.pose(), u0, u1, tint));
        poseStack.popPose();
    }

    static void submitBurst(PoseStack poseStack, SubmitNodeCollector collector, int rays, float reach, float spinDegrees, int coreColor, int tipColor) {
        int count = Math.min(rays, MAX_RAYS);
        if (count <= 0 || reach <= 0.0F) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(BURST_TILT));
        poseStack.mulPose(Axis.YP.rotationDegrees(spinDegrees));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeBurst(buffer, pose.pose(), count, reach, coreColor, tipColor));
        poseStack.popPose();
    }

    private static void writeBurst(VertexConsumer buffer, Matrix4fc matrix, int count, float reach, int coreColor, int tipColor) {
        for (int ray = 0; ray < count; ray++) {
            int base = ray * RAY_STRIDE;
            for (int corner = 0; corner < TIP_CORNERS; corner++) {
                int from = base + corner * COMPONENTS;
                int to = base + (corner + 1) % TIP_CORNERS * COMPONENTS;
                buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor);
                buffer.addVertex(matrix, RAY_TIPS[to] * reach, RAY_TIPS[to + 1] * reach, RAY_TIPS[to + 2] * reach).setColor(tipColor);
                buffer.addVertex(matrix, RAY_TIPS[from] * reach, RAY_TIPS[from + 1] * reach, RAY_TIPS[from + 2] * reach).setColor(tipColor);
            }
        }
    }

    private static void writeCore(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -CORE_HALF, -CORE_HALF, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, CORE_HALF, -CORE_HALF, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, CORE_HALF, CORE_HALF, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -CORE_HALF, CORE_HALF, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    private static float[] buildRayTips() {
        float[] tips = new float[MAX_RAYS * RAY_STRIDE];
        for (int ray = 0; ray < MAX_RAYS; ray++) {
            double u = fraction(HALF + ray * SPREAD_STEP_U);
            double v = fraction(HALF + ray * SPREAD_STEP_V);
            double dy = 1.0 - 2.0 * v;
            double ring = Math.sqrt(Math.max(0.0, 1.0 - dy * dy));
            double angle = u * Mth.TWO_PI;
            double dx = Math.cos(angle) * ring;
            double dz = Math.sin(angle) * ring;
            double length = SHORTEST_RAY + (1.0 - SHORTEST_RAY) * fraction(LENGTH_SEED + ray * LENGTH_STEP);
            double width = THINNEST_RAY + WIDTH_SPREAD * fraction(WIDTH_SEED + ray * WIDTH_STEP);
            double upX = Math.abs(dy) < VERTICAL_THRESHOLD ? 0.0 : 1.0;
            double upY = Math.abs(dy) < VERTICAL_THRESHOLD ? 1.0 : 0.0;
            double along = upX * dx + upY * dy;
            double px = upX - along * dx;
            double py = upY - along * dy;
            double pz = -along * dz;
            double pLength = Math.sqrt(px * px + py * py + pz * pz);
            px /= pLength;
            py /= pLength;
            pz /= pLength;
            double qx = dy * pz - dz * py;
            double qy = dz * px - dx * pz;
            double qz = dx * py - dy * px;
            for (int corner = 0; corner < TIP_CORNERS; corner++) {
                double turn = corner * Mth.TWO_PI / TIP_CORNERS;
                double cos = Math.cos(turn) * width;
                double sin = Math.sin(turn) * width;
                int at = ray * RAY_STRIDE + corner * COMPONENTS;
                tips[at] = (float) (dx * length + px * cos + qx * sin);
                tips[at + 1] = (float) (dy * length + py * cos + qy * sin);
                tips[at + 2] = (float) (dz * length + pz * cos + qz * sin);
            }
        }
        return tips;
    }

    private static double fraction(double value) {
        return value - Math.floor(value);
    }

    public static final class State extends EntityRenderState {
        public float centre;
    }
}
