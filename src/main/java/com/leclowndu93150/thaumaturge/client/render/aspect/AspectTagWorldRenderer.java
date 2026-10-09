package com.leclowndu93150.thaumaturge.client.render.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer.BlendMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class AspectTagWorldRenderer {
    public static final float DEFAULT_SCALE = 0.0625F;
    public static final Identifier UNKNOWN_TEXTURE = TTIds.rl("textures/aspects/_unknown.png");

    private static final float HALF = 0.5F;
    private static final int ROW_SIZE = 5;
    private static final float SPACING_FACTOR = 4.0F;
    private static final float ROW_HEIGHT_FACTOR = 1.05F;
    private static final float FACE_REACH_FACTOR = 2.0F;
    private static final float UNKNOWN_ALPHA = 0.75F;
    private static final float TEXT_PIXEL = 0.04F;
    private static final float AMOUNT_RIGHT = 0.52F;
    private static final float AMOUNT_TOP = 0.24F;
    private static final float DARK_LIFT = 0.004F;
    private static final float WHITE_LIFT = 0.008F;
    private static final int AMOUNT_COLOR = 0xFFFFFFFF;
    private static final int AMOUNT_SHADOW_COLOR = 0xFF111111;
    private static final int NO_BACKGROUND = 0;
    private static final boolean[] SHADOW_THEN_FACE = {true, false};
    private static final float NORMAL_Z = -1.0F;
    private static final Predicate<Holder<IAspect>> ALL_DISCOVERED = aspect -> true;
    private static final float[][] QUAD_CORNERS = {{-HALF, -HALF, 0.0F, 1.0F}, {HALF, -HALF, 1.0F, 1.0F}, {HALF, HALF, 1.0F, 0.0F}, {-HALF, HALF, 0.0F, 0.0F}};

    private AspectTagWorldRenderer() {}

    public static void renderTagCloud(PoseStack poseStack, Minecraft minecraft, double x, double y, double z, AspectList aspects, @Nullable Direction face, float scale, float alpha) {
        renderTagCloud(poseStack, minecraft, x, y, z, aspects, face, scale, alpha, ALL_DISCOVERED, true);
    }

    public static void renderTagCloud(PoseStack poseStack, Minecraft minecraft, double x, double y, double z, AspectList aspects, @Nullable Direction face, float scale, float alpha, Predicate<Holder<IAspect>> discovered) {
        renderTagCloud(poseStack, minecraft, x, y, z, aspects, face, scale, alpha, discovered, true);
    }

    public static void renderTagCloud(PoseStack poseStack, Minecraft minecraft, double x, double y, double z, AspectList aspects, @Nullable Direction face, float scale, float alpha, Predicate<Holder<IAspect>> discovered, boolean showAmounts) {
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        double baseX = face == null ? x - HALF : x;
        double baseZ = face == null ? z - HALF : z;
        Vec3 anchor = cloudAnchor(baseX, y, baseZ, face, scale);
        double bearing = Math.atan2(camera.x - (baseX + HALF), camera.z - (baseZ + HALF));
        Quaternionf facing = new Quaternionf().rotationY((float) bearing);
        Vec3 right = new Vec3(Math.cos(bearing), 0.0, -Math.sin(bearing));
        List<AspectInstance> tags = aspects.sortedByTag();
        for (int index = 0; index < tags.size(); index++) {
            int row = index / ROW_SIZE;
            double slide = rowSlide(index % ROW_SIZE, Math.min(ROW_SIZE, tags.size() - row * ROW_SIZE), scale);
            Vec3 at = anchor.add(right.scale(slide)).add(0.0, row * ROW_HEIGHT_FACTOR * scale, 0.0);
            poseStack.pushPose();
            poseStack.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
            poseStack.mulPose(facing);
            drawTag(poseStack, buffers, minecraft.font, tags.get(index), scale, alpha, discovered, showAmounts);
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    private static double rowSlide(int column, int rowCount, float scale) {
        return (column - rowCount / 2.0 + HALF) * SPACING_FACTOR * scale * scale;
    }

    private static RenderType billboardType(Identifier texture, BlendMode blend) {
        return switch (blend) {
            case ADDITIVE -> TTFlatRenderTypes.entityAdditiveFlat(texture);
            case ALPHA -> TTFlatRenderTypes.entityTranslucentFlat(texture);
        };
    }

    private static Vec3 cloudAnchor(double baseX, double baseY, double baseZ, @Nullable Direction face, float scale) {
        Vec3 center = new Vec3(baseX + HALF, baseY + HALF, baseZ + HALF);
        if (face == null) {
            return center;
        }
        double reach = FACE_REACH_FACTOR * scale;
        return center.add(face.getStepX() * reach, face.getStepY() * reach, face.getStepZ() * reach);
    }

    public static void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, Holder<IAspect> aspect, float scale, float alpha, boolean monochrome, int packedLight) {
        renderBillboard(poseStack, buffers, aspect, scale, alpha, monochrome, packedLight, BlendMode.ALPHA);
    }

    public static void renderBillboardAdditive(PoseStack poseStack, MultiBufferSource buffers, Holder<IAspect> aspect, float scale, float alpha, boolean monochrome, int packedLight) {
        renderBillboard(poseStack, buffers, aspect, scale, alpha, monochrome, packedLight, BlendMode.ADDITIVE);
    }

    public static void renderBillboard(PoseStack poseStack, MultiBufferSource buffers, @Nullable Holder<IAspect> aspect, float scale, float alpha, boolean monochrome, int packedLight, BlendMode blend) {
        if (aspect == null || !aspect.isBound()) {
            return;
        }
        RenderType renderType = billboardType(aspect.value().texture(), blend);
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        drawScaledQuad(poseStack, buffers.getBuffer(renderType), scale, AspectTagRenderer.colorOf(aspect.value(), alpha, monochrome), packedLight);
        poseStack.popPose();
    }

    public static void renderQuad(PoseStack poseStack, VertexConsumer buffer, Holder<IAspect> aspect, float alpha, boolean monochrome, int packedLight) {
        renderQuad(poseStack.last(), buffer, aspect, alpha, monochrome, packedLight);
    }

    public static void renderQuad(PoseStack.Pose pose, VertexConsumer buffer, @Nullable Holder<IAspect> aspect, float alpha, boolean monochrome, int packedLight) {
        if (aspect == null || !aspect.isBound()) {
            return;
        }
        renderQuad(pose, buffer, AspectTagRenderer.colorOf(aspect.value(), alpha, monochrome), packedLight);
    }

    public static void renderQuad(PoseStack.Pose pose, VertexConsumer buffer, int color, int packedLight) {
        for (float[] corner : QUAD_CORNERS) {
            quadVertex(pose, buffer, corner, color, packedLight);
        }
    }

    private static void drawScaledQuad(PoseStack poseStack, VertexConsumer buffer, float scale, int color, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        renderQuad(poseStack.last(), buffer, color, packedLight);
        poseStack.popPose();
    }

    private static void drawTag(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Font font, AspectInstance entry, float scale, float alpha, Predicate<Holder<IAspect>> discovered, boolean showAmounts) {
        Holder<IAspect> aspect = entry.aspect();
        boolean known = discovered.test(aspect);
        Identifier texture = known ? aspect.value().texture() : UNKNOWN_TEXTURE;
        int color = AspectTagRenderer.colorOf(aspect.value(), known ? alpha : UNKNOWN_ALPHA, false);
        drawScaledQuad(poseStack, buffers.getBuffer(TTFlatRenderTypes.entityTranslucentFlat(texture)), scale, color, LightCoordsUtil.FULL_BRIGHT);
        if (showAmounts) {
            drawAmount(poseStack, buffers, font, Integer.toString(entry.amount()), scale);
        }
    }

    private static void drawAmount(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Font font, String text, float scale) {
        float pixel = TEXT_PIXEL * scale;
        float width = font.width(text);
        for (boolean shadow : SHADOW_THEN_FACE) {
            float nudgeX = shadow ? pixel : 0.0F;
            float nudgeY = shadow ? -pixel : 0.0F;
            float lift = (shadow ? DARK_LIFT : WHITE_LIFT) * scale;
            drawDigits(poseStack, buffers, font, text, width, AMOUNT_RIGHT * scale + nudgeX, -AMOUNT_TOP * scale + nudgeY, lift, pixel, shadow ? AMOUNT_SHADOW_COLOR : AMOUNT_COLOR);
        }
    }

    private static void drawDigits(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Font font, String text, float width, float right, float top, float lift, float pixel, int color) {
        Matrix4f matrix = new Matrix4f(poseStack.last().pose()).translate(right, top, lift).scale(pixel, -pixel, pixel);
        font.drawInBatch(text, -width, 0.0F, color, false, matrix, buffers, Font.DisplayMode.NORMAL, NO_BACKGROUND, LightCoordsUtil.FULL_BRIGHT);
    }

    private static void quadVertex(PoseStack.Pose pose, VertexConsumer buffer, float[] corner, int color, int light) {
        buffer.addVertex(pose, corner[0], corner[1], 0.0F).setUv(corner[2], corner[3]).setColor(color).setNormal(pose, 0.0F, 0.0F, NORMAL_Z).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
    }
}
