package com.leclowndu93150.thaumcraft.client.render.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;

public final class AspectTagWorldRenderer {
    public static final float DEFAULT_SCALE = 0.0625F;
    private static final float HALF_QUAD = 0.5F;

    private AspectTagWorldRenderer() {}

    public static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean bw,
            int packedLight
    ) {
        renderBillboard(poseStack, buffers, aspect, scale, alpha, bw, packedLight, AspectTagRenderer.BlendMode.ALPHA);
    }

    public static void renderBillboardAdditive(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean bw,
            int packedLight
    ) {
        renderBillboard(poseStack, buffers, aspect, scale, alpha, bw, packedLight, AspectTagRenderer.BlendMode.ADDITIVE);
    }

    public static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean bw,
            int packedLight,
            AspectTagRenderer.BlendMode blend
    ) {
        if (aspect == null || aspect.value() == null) return;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        IAspect value = aspect.value();
        int color = AspectTagRenderer.colorOf(value, alpha, bw);
        RenderType type = blend == AspectTagRenderer.BlendMode.ADDITIVE
                ? RenderTypes.entityTranslucentEmissive(value.texture())
                : RenderTypes.entityTranslucent(value.texture());
        VertexConsumer buffer = buffers.getBuffer(type);
        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        poseStack.scale(scale, scale, scale);
        PoseStack.Pose pose = poseStack.last();
        addQuadVertex(buffer, pose, -HALF_QUAD, -HALF_QUAD, 0.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, -HALF_QUAD, 1.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, HALF_QUAD, 1.0F, 0.0F, color, packedLight);
        addQuadVertex(buffer, pose, -HALF_QUAD, HALF_QUAD, 0.0F, 0.0F, color, packedLight);
        poseStack.popPose();
    }

    public static void renderQuad(
            PoseStack poseStack,
            VertexConsumer buffer,
            Holder<IAspect> aspect,
            float alpha,
            boolean bw,
            int packedLight
    ) {
        if (aspect == null || aspect.value() == null) return;
        IAspect value = aspect.value();
        int color = AspectTagRenderer.colorOf(value, alpha, bw);
        PoseStack.Pose pose = poseStack.last();
        addQuadVertex(buffer, pose, -HALF_QUAD, -HALF_QUAD, 0.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, -HALF_QUAD, 1.0F, 1.0F, color, packedLight);
        addQuadVertex(buffer, pose, HALF_QUAD, HALF_QUAD, 1.0F, 0.0F, color, packedLight);
        addQuadVertex(buffer, pose, -HALF_QUAD, HALF_QUAD, 0.0F, 0.0F, color, packedLight);
    }

    private static void addQuadVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            int color,
            int packedLight
    ) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
