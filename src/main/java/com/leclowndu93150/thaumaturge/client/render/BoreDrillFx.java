package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BoreBeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class BoreDrillFx {
    private static final double TIP_FORWARD = 0.5;
    private static final double TIP_RISE = 0.075;
    private static final float SCROLL_PER_TICK = 0.2F;
    private static final float SCROLL_WRAP = 2.0F;
    private static final float SCROLL_BASE = -1.0F;
    private static final float SPIN_DEGREES_PER_TICK = 5.0F;
    private static final float FULL_CIRCLE_DEGREES = 360.0F;
    private static final int STRIPS = 3;
    private static final float STRIP_STEP_DEGREES = 120.0F;
    private static final float CONE_HALF_WIDTH = 0.15F;
    private static final float CONE_LENGTH = 5.0F;
    private static final float CONE_TEXTURE_REPEATS = 5.0F;
    private static final float CONE_OPACITY = 0.4F;
    private static final int CONE_LIGHT = 0xC8;
    private static final float FLARE_OPACITY = 0.8F;
    private static final float FLARE_HALF_SIZE = 0.5F;
    private static final int FLARE_FRAMES = 32;
    private static final int FLARE_ROW = 3;
    private static final float SHEET_CELLS = 32.0F;
    private static final float MINT_RED = 0.0F;
    private static final float MINT_GREEN = 1.0F;
    private static final float MINT_BLUE = 0.4F;
    private static final double VIEW_RANGE = 64.0;
    private static final double VIEW_RANGE_FAST = 32.0;

    private BoreDrillFx() {}

    public static float beamUvScroll(float ticks) {
        return ticks * SCROLL_PER_TICK % SCROLL_WRAP;
    }

    public static float beamSpin(long gameTime, float partialTick) {
        long period = (long) (FULL_CIRCLE_DEGREES / SPIN_DEGREES_PER_TICK);
        return ((gameTime % period) + partialTick) * SPIN_DEGREES_PER_TICK;
    }

    public static int tipFrame(int ticks) {
        return Math.floorMod(ticks, FLARE_FRAMES);
    }

    public static Vec3 tipOffset(float yawDegrees, float pitchDegrees, float eyeHeight) {
        Vec3 aim = Vec3.directionFromRotation(pitchDegrees, yawDegrees);
        Vec3 up = upOf(yawDegrees, pitchDegrees);
        return new Vec3(aim.x * TIP_FORWARD + up.x * TIP_RISE, eyeHeight + aim.y * TIP_FORWARD + up.y * TIP_RISE, aim.z * TIP_FORWARD + up.z * TIP_RISE);
    }

    private static Vec3 upOf(float yawDegrees, float pitchDegrees) {
        double yaw = yawDegrees * Mth.DEG_TO_RAD;
        double pitch = pitchDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Math.sin(yaw) * Math.sin(pitch), Math.cos(pitch), Math.cos(yaw) * Math.sin(pitch));
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vec3 tip, float yaw, float pitch, float uvScroll, float spin, int tipFrame) {
        if (beyondView(poseStack, tip)) {
            return;
        }
        Vec3 aim = Vec3.directionFromRotation(pitch, yaw);
        poseStack.pushPose();
        poseStack.translate(tip.x, tip.y, tip.z);
        collector.submitCustomGeometry(poseStack, BoreBeamRenderType.DRILL_BEAM, (pose, buffer) -> writeCone(buffer, pose.pose(), aim, upOf(yaw, pitch), yaw, uvScroll, spin));
        poseStack.mulPose(camera.orientation);
        int frame = Math.floorMod(tipFrame, FLARE_FRAMES);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(BeamRenderType.NODE), (pose, buffer) -> writeFlare(buffer, pose.pose(), frame));
        poseStack.popPose();
    }

    private static boolean beyondView(PoseStack poseStack, Vec3 tip) {
        Vector3f relative = poseStack.last().pose().transformPosition((float) tip.x, (float) tip.y, (float) tip.z, new Vector3f());
        double range = Minecraft.getInstance().options.graphicsPreset().get() == GraphicsPreset.FAST ? VIEW_RANGE_FAST : VIEW_RANGE;
        return relative.lengthSquared() > range * range;
    }

    private static void writeCone(VertexConsumer buffer, Matrix4fc matrix, Vec3 aim, Vec3 up, float yawDegrees, float uvScroll, float spin) {
        double yaw = yawDegrees * Mth.DEG_TO_RAD;
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        float endX = (float) (aim.x * CONE_LENGTH);
        float endY = (float) (aim.y * CONE_LENGTH);
        float endZ = (float) (aim.z * CONE_LENGTH);
        for (int strip = 0; strip < STRIPS; strip++) {
            double angle = (spin + STRIP_STEP_DEGREES * (strip + 1)) * Mth.DEG_TO_RAD;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            float ex = (float) ((cos * rx + sin * up.x) * CONE_HALF_WIDTH);
            float ey = (float) (sin * up.y * CONE_HALF_WIDTH);
            float ez = (float) ((cos * rz + sin * up.z) * CONE_HALF_WIDTH);
            float v0 = SCROLL_BASE + uvScroll + (float) strip / STRIPS;
            float v1 = v0 + CONE_TEXTURE_REPEATS;
            coneVertex(buffer, matrix, -ex, -ey, -ez, 1.0F, v0);
            coneVertex(buffer, matrix, ex, ey, ez, 0.0F, v0);
            coneVertex(buffer, matrix, endX, endY, endZ, 0.0F, v1);
            coneVertex(buffer, matrix, endX, endY, endZ, 1.0F, v1);
        }
    }

    private static void coneVertex(VertexConsumer buffer, Matrix4fc matrix, float x, float y, float z, float u, float v) {
        buffer.addVertex(matrix, x, y, z).setUv(u, v).setColor(MINT_RED, MINT_GREEN, MINT_BLUE, CONE_OPACITY).setLight(CONE_LIGHT);
    }

    private static void writeFlare(VertexConsumer buffer, Matrix4fc matrix, int frame) {
        float u0 = frame / SHEET_CELLS;
        float u1 = (frame + 1) / SHEET_CELLS;
        float v0 = FLARE_ROW / SHEET_CELLS;
        float v1 = (FLARE_ROW + 1) / SHEET_CELLS;
        flareVertex(buffer, matrix, -FLARE_HALF_SIZE, -FLARE_HALF_SIZE, u1, v1);
        flareVertex(buffer, matrix, FLARE_HALF_SIZE, -FLARE_HALF_SIZE, u0, v1);
        flareVertex(buffer, matrix, FLARE_HALF_SIZE, FLARE_HALF_SIZE, u0, v0);
        flareVertex(buffer, matrix, -FLARE_HALF_SIZE, FLARE_HALF_SIZE, u1, v0);
    }

    private static void flareVertex(VertexConsumer buffer, Matrix4fc matrix, float x, float y, float u, float v) {
        buffer.addVertex(matrix, x, y, 0.0F).setUv(u, v).setColor(MINT_RED, MINT_GREEN, MINT_BLUE, FLARE_OPACITY).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
