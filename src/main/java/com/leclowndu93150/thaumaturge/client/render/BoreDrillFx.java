package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BoreBeamRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BoreDrillFx {
    private static final float SCROLL_PER_TICK = 0.2F;
    private static final float SCROLL_WRAP = 2.0F;
    private static final long SPIN_PERIOD_TICKS = 72L;
    private static final float SPIN_DEGREES_PER_TICK = 5.0F;
    private static final int TIP_FRAME_BASE = 96;
    private static final int TIP_FRAME_CYCLE = 32;
    private static final int TIP_SHEET_COLUMNS = 32;
    private static final float TIP_FORWARD = 0.5F;
    private static final float TIP_RAISE = 0.075F;
    private static final int STRIP_COUNT = 3;
    private static final float STRIP_SPREAD_DEGREES = 120.0F;
    private static final float STRIP_LENGTH = 5.0F;
    private static final float STRIP_HALF_WIDTH = 0.15F;
    private static final float STRIP_V_START = -1.0F;
    private static final float BEAM_RED = 0.0F;
    private static final float BEAM_GREEN = 1.0F;
    private static final float BEAM_BLUE = 0.4F;
    private static final float BEAM_ALPHA = 0.4F;
    private static final float FLARE_ALPHA = 0.8F;
    private static final float FLARE_HALF = 0.5F;
    private static final int BEAM_LIGHT = 0xC8;

    private BoreDrillFx() {}

    public static float beamUvScroll(float ticks) {
        return ticks * SCROLL_PER_TICK;
    }

    public static float beamSpin(long gameTime, float partialTick) {
        return (gameTime % SPIN_PERIOD_TICKS) * SPIN_DEGREES_PER_TICK + SPIN_DEGREES_PER_TICK * partialTick;
    }

    public static int tipFrame(int ticks) {
        return TIP_FRAME_BASE + Math.floorMod(ticks, TIP_FRAME_CYCLE);
    }

    public static Vec3 tipOffset(float yawDegrees, float pitchDegrees, float eyeHeight) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        float pitch = pitchDegrees * Mth.DEG_TO_RAD;
        float reach = TIP_FORWARD * Mth.cos(pitch) + TIP_RAISE * Mth.sin(pitch);
        return new Vec3(-Mth.sin(yaw) * reach, eyeHeight + TIP_RAISE * Mth.cos(pitch) - TIP_FORWARD * Mth.sin(pitch), Mth.cos(yaw) * reach);
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vec3 tip, float yaw, float pitch, float uvScroll, float spin, int tipFrame) {
        submitBeams(poseStack, collector, tip, yaw, pitch, uvScroll, spin);
        submitFlare(poseStack, collector, camera, tip, tipFrame);
    }

    private static void submitBeams(PoseStack poseStack, SubmitNodeCollector collector, Vec3 tip, float yaw, float pitch, float uvScroll, float spin) {
        float scroll = uvScroll - Mth.floor(uvScroll / SCROLL_WRAP) * SCROLL_WRAP;
        int color = ARGB.colorFromFloat(BEAM_ALPHA, BEAM_RED, BEAM_GREEN, BEAM_BLUE);
        poseStack.pushPose();
        poseStack.translate(tip.x, tip.y, tip.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        for (int strip = 0; strip < STRIP_COUNT; strip++) {
            float nearV = STRIP_V_START + scroll + (float) strip / STRIP_COUNT;
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(strip * STRIP_SPREAD_DEGREES));
            collector.submitCustomGeometry(poseStack, BoreBeamRenderType.DRILL_BEAM, (pose, buffer) -> writeStrip(pose, buffer, nearV, color));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void writeStrip(PoseStack.Pose pose, VertexConsumer buffer, float nearV, int color) {
        float farV = nearV + STRIP_LENGTH;
        stripVertex(pose, buffer, -STRIP_HALF_WIDTH, 0.0F, 1.0F, nearV, color);
        stripVertex(pose, buffer, STRIP_HALF_WIDTH, 0.0F, 0.0F, nearV, color);
        stripVertex(pose, buffer, 0.0F, STRIP_LENGTH, 0.0F, farV, color);
        stripVertex(pose, buffer, 0.0F, STRIP_LENGTH, 1.0F, farV, color);
    }

    private static void stripVertex(PoseStack.Pose pose, VertexConsumer buffer, float across, float along, float u, float v, int color) {
        buffer.addVertex(pose, across, 0.0F, along).setUv(u, v).setColor(color).setLight(BEAM_LIGHT);
    }

    private static void submitFlare(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vec3 tip, int tipFrame) {
        float cell = 1.0F / TIP_SHEET_COLUMNS;
        float uMin = (tipFrame % TIP_SHEET_COLUMNS) * cell;
        float vMin = (tipFrame / TIP_SHEET_COLUMNS) * cell;
        int color = ARGB.colorFromFloat(FLARE_ALPHA, BEAM_RED, BEAM_GREEN, BEAM_BLUE);
        poseStack.pushPose();
        poseStack.translate(tip.x, tip.y, tip.z);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, BeamRenderType.NODE_TYPE, (pose, buffer) -> writeFlare(pose, buffer, uMin, uMin + cell, vMin, vMin + cell, color));
        poseStack.popPose();
    }

    private static void writeFlare(PoseStack.Pose pose, VertexConsumer buffer, float uMin, float uMax, float vMin, float vMax, int color) {
        flareVertex(pose, buffer, -FLARE_HALF, -FLARE_HALF, uMax, vMax, color);
        flareVertex(pose, buffer, -FLARE_HALF, FLARE_HALF, uMax, vMin, color);
        flareVertex(pose, buffer, FLARE_HALF, FLARE_HALF, uMin, vMin, color);
        flareVertex(pose, buffer, FLARE_HALF, -FLARE_HALF, uMin, vMax, color);
    }

    private static void flareVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v, int color) {
        buffer.addVertex(pose, x, y, 0.0F).setUv(u, v).setColor(color);
    }
}
