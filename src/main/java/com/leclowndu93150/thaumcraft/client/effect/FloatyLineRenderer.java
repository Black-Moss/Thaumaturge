package com.leclowndu93150.thaumcraft.client.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.effect.pipeline.TCRenderPipelines;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FloatyLineRenderer {
    private static final Identifier WISPY_TEXTURE = TCIds.rl("textures/misc/wispy.png");

    private static final RenderType WISPY_LINE = RenderType.create("tc_wispy_line",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE)
                    .withTexture("Sampler0", WISPY_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());

    private static final float SEGMENTS_PER_BLOCK = 2.0F;
    private static final float TIME_UNITS_PER_TICK = 50.0F / 30.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float END_ATTACH_START = 0.7F;
    private static final float END_ATTACH_ALPHA = 0.8F;

    private FloatyLineRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Vec3 fromRelative,
                              float time, int color, float speed, float distanceFraction, float width) {
        PoseStack.Pose pose = poseStack.last().copy();
        collector.submitCustomGeometry(poseStack, WISPY_LINE,
                (p, buffer) -> write(pose, buffer, fromRelative, time, color, speed, distanceFraction, width));
    }

    public static void draw(PoseStack poseStack, MultiBufferSource buffers, Vec3 fromRelative,
                            float time, int color, float speed, float distanceFraction, float width) {
        write(poseStack.last(), buffers.getBuffer(WISPY_LINE), fromRelative, time, color, speed,
                distanceFraction, width);
    }

    private static void write(PoseStack.Pose pose, VertexConsumer buffer, Vec3 fromRelative,
                              float time, int color, float speed, float distanceFraction, float width) {
        float dist = (float) fromRelative.length();
        if (dist < 0.1F) {
            return;
        }
        float blocks = Math.round(dist);
        float length = blocks * SEGMENTS_PER_BLOCK;
        if (length < 2.0F) {
            length = 2.0F;
        }
        int count = (int) (length * distanceFraction);
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float finalLength = length;
        {
            for (int axis = 0; axis < 2; axis++) {
                float wx = axis == 0 ? 0.0F : width;
                float wy = axis == 0 ? width : 0.0F;
                float prevX = 0.0F;
                float prevY = 0.0F;
                float prevZ = 0.0F;
                float prevU = 0.0F;
                float prevAlpha = 0.0F;
                boolean hasPrev = false;
                for (int i = 0; i <= count; i++) {
                    float f2 = i / finalLength;
                    float mid = 1.0F - Math.abs(i - finalLength / 2.0F) / (finalLength / 2.0F);
                    float wobblePhase = dist * (1.0F - f2) * SEGMENTS_PER_BLOCK - time / 5.0F;
                    float dx = (float) fromRelative.x + Mth.sin(wobblePhase / 4.0F) * 0.5F * mid;
                    float dy = (float) fromRelative.y + Mth.sin(wobblePhase / 3.0F) * 0.5F * mid;
                    float dz = (float) fromRelative.z + Mth.sin(wobblePhase / 2.0F) * 0.5F * mid;
                    float px = dx * f2;
                    float py = dy * f2;
                    float pz = dz * f2;
                    float u = (1.0F - f2) * dist - time * speed;
                    float alpha = Math.max(0.0F, mid);
                    if (f2 > END_ATTACH_START) {
                        alpha = Math.max(alpha,
                                (f2 - END_ATTACH_START) / (1.0F - END_ATTACH_START) * END_ATTACH_ALPHA);
                    }
                    if (hasPrev) {
                        int c0 = ARGB.colorFromFloat(prevAlpha, red, green, blue);
                        int c1 = ARGB.colorFromFloat(alpha, red, green, blue);
                        buffer.addVertex(pose, prevX + wx, prevY - wy, prevZ).setColor(c0).setUv(prevU, 1.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(EMISSIVE_LIGHT).setNormal(pose, 0, 1, 0);
                        buffer.addVertex(pose, prevX - wx, prevY + wy, prevZ).setColor(c0).setUv(prevU, 0.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(EMISSIVE_LIGHT).setNormal(pose, 0, 1, 0);
                        buffer.addVertex(pose, px - wx, py + wy, pz).setColor(c1).setUv(u, 0.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(EMISSIVE_LIGHT).setNormal(pose, 0, 1, 0);
                        buffer.addVertex(pose, px + wx, py - wy, pz).setColor(c1).setUv(u, 1.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(EMISSIVE_LIGHT).setNormal(pose, 0, 1, 0);
                    }
                    prevX = px;
                    prevY = py;
                    prevZ = pz;
                    prevU = u;
                    prevAlpha = alpha;
                    hasPrev = true;
                }
            }
        }
    }

    public static float time(long gameTime, float partialTicks) {
        return (gameTime + partialTicks) * TIME_UNITS_PER_TICK;
    }
}
