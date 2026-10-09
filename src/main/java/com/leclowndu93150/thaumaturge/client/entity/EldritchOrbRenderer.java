package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb, EldritchOrbRenderer.State> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final int RAY_COUNT = 12;
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_PERIOD_TICKS = 80.0F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final float BILLBOARD_SCALE = 0.75F;
    private static final float BILLBOARD_HALF = 0.5F;
    private static final int BILLBOARD_TINT = ARGB.white(1.0F);
    private static final int RAY_APEX_COLOR = ARGB.white(1.0F);
    private static final int RAY_BASE_COLOR = ARGB.colorFromFloat(0.0F, 0.75F, 0.75F, 0.75F);
    private static final float HEIGHT_VARIANCE = 20.0F;
    private static final float HEIGHT_MINIMUM = 5.0F;
    private static final float WIDTH_VARIANCE = 2.0F;
    private static final float WIDTH_MINIMUM = 1.0F;
    private static final float RAY_SCALE_DIVISOR = 30.0F;
    private static final float CORNER_SIDE = 0.866F;
    private static final float CORNER_BACK = 0.5F;
    private static final float GOLDEN_ANGLE_DEGREES = 137.50777F;
    private static final float HEIGHT_SEQUENCE_STEP = 0.6180340F;
    private static final float WIDTH_SEQUENCE_STEP = 0.7548777F;
    private static final float[][] BILLBOARD_CORNERS = {{-1.0F, -1.0F, 0.0F}, {1.0F, -1.0F, 1.0F}, {1.0F, 1.0F, 1.0F}, {-1.0F, 1.0F, 0.0F}};

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
        state.ticks = state.ageInTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float grow = Math.min(state.ticks, GROW_TICKS) / GROW_TICKS;
        float spin = state.ticks / SPIN_PERIOD_TICKS * FULL_TURN_DEGREES;
        submitBurst(poseStack, collector, RAY_COUNT, grow, spin, RAY_BASE_COLOR);
        int frame = Math.floorMod(Mth.floor(state.ticks), ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        poseStack.scale(BILLBOARD_SCALE, BILLBOARD_SCALE, BILLBOARD_SCALE);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.translucent(ParticleTextures.ELDRITCH_ORB), (pose, buffer) -> writeBillboard(buffer, pose.pose(), u0, u1));
        poseStack.popPose();
    }

    static void submitBurst(PoseStack poseStack, SubmitNodeCollector collector, int count, float grow, float spinDegrees, int baseColor) {
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeBurst(buffer, pose.pose(), count, grow, spinDegrees, baseColor));
    }

    private static void writeBurst(VertexConsumer buffer, Matrix4fc origin, int count, float grow, float spinDegrees, int baseColor) {
        Matrix4f scratch = new Matrix4f();
        Matrix4f spun = new Matrix4f(origin).rotateY(spinDegrees * Mth.DEG_TO_RAD);
        for (int i = 0; i < count; i++) {
            orient(scratch.set(spun), i, count);
            float height = extent(i, HEIGHT_SEQUENCE_STEP, HEIGHT_VARIANCE, HEIGHT_MINIMUM, grow);
            float width = extent(i, WIDTH_SEQUENCE_STEP, WIDTH_VARIANCE, WIDTH_MINIMUM, grow);
            writePyramid(buffer, scratch, height, width, baseColor);
        }
    }

    private static void orient(Matrix4f target, int index, int count) {
        float polar = (float) Math.acos(1.0F - 2.0F * (index + 0.5F) / count);
        target.rotateY(index * GOLDEN_ANGLE_DEGREES * Mth.DEG_TO_RAD).rotateX(polar);
    }

    private static float extent(int index, float step, float variance, float minimum, float grow) {
        return (Mth.frac((index + 1) * step) * variance + minimum) / RAY_SCALE_DIVISOR * grow;
    }

    private static void writePyramid(VertexConsumer buffer, Matrix4fc matrix, float height, float width, int baseColor) {
        float side = CORNER_SIDE * width;
        float back = CORNER_BACK * width;
        writeFace(buffer, matrix, height, baseColor, -side, -back, side, -back);
        writeFace(buffer, matrix, height, baseColor, side, -back, 0.0F, width);
        writeFace(buffer, matrix, height, baseColor, 0.0F, width, -side, -back);
    }

    private static void writeFace(VertexConsumer buffer, Matrix4fc matrix, float height, int baseColor, float x0, float z0, float x1, float z1) {
        buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(RAY_APEX_COLOR);
        buffer.addVertex(matrix, x0, height, z0).setColor(baseColor);
        buffer.addVertex(matrix, x1, height, z1).setColor(baseColor);
    }

    private static void writeBillboard(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1) {
        for (int corner = 0; corner < BILLBOARD_CORNERS.length; corner++) {
            float[] spec = BILLBOARD_CORNERS[corner];
            float u = spec[2] == 0.0F ? u0 : u1;
            float v = spec[1] < 0.0F ? StripUv.V1 : StripUv.V0;
            buffer.addVertex(matrix, spec[0] * BILLBOARD_HALF, spec[1] * BILLBOARD_HALF, 0.0F).setUv(u, v).setColor(BILLBOARD_TINT).setLight(LightCoordsUtil.FULL_BRIGHT);
        }
    }

    public static final class State extends EntityRenderState {
        public float ticks;
    }
}
