package com.leclowndu93150.thaumaturge.client.render.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer.BlendMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class AspectTagWorldRenderer {
    public static final float DEFAULT_SCALE = 0.0625F;
    public static final Identifier UNKNOWN_TEXTURE = TTIds.rl("textures/aspects/_unknown.png");

    private static final float HALF = 0.5F;
    private static final int ROW_SIZE = 5;
    private static final int AMOUNT_COLOR = 0xFFFFFFFF;
    private static final int NO_BACKGROUND = 0;
    private static final float NORMAL_Z = -1.0F;
    private static final Predicate<Holder<IAspect>> ALL_DISCOVERED = aspect -> true;
    private static final float TAG_SIZE_FACTOR = 0.6F;
    private static final float SPACING_FACTOR = 1.2F;
    private static final float ROW_HEIGHT_FACTOR = 1.05F;
    private static final float FACE_REACH_FACTOR = 2.0F;
    private static final float TEXT_PIXEL = 0.04F;
    private static final float AMOUNT_RIGHT = HALF;
    private static final float AMOUNT_BOTTOM = -HALF;
    private static final float AMOUNT_PEN_OVERHANG = 0.5F;
    private static final float AMOUNT_TEXT_TOP = -6.5F;
    private static final int SHADOW_STEP = 1;
    private static final float DARK_LIFT = 0.002F;
    private static final float WHITE_LIFT = 0.004F;
    private static final int AMOUNT_SHADOW_COLOR = 0xFF111111;
    private static final float UNKNOWN_ALPHA = 0.75F;
    private static final float[][] QUAD_CORNERS = {{-HALF, -HALF, 0.0F, 1.0F}, {HALF, -HALF, 1.0F, 1.0F}, {HALF, HALF, 1.0F, 0.0F}, {-HALF, HALF, 0.0F, 0.0F}};

    private AspectTagWorldRenderer() {}

    public static void renderTagCloud(PoseStack poseStack, Minecraft minecraft, double x, double y, double z, AspectList aspects, @Nullable Direction face, float scale, float alpha) {
        renderTagCloud(poseStack, minecraft, x, y, z, aspects, face, scale, alpha, ALL_DISCOVERED);
    }

    public static void renderTagCloud(PoseStack poseStack, Minecraft minecraft, double x, double y, double z, AspectList aspects, @Nullable Direction face, float scale, float alpha, Predicate<Holder<IAspect>> discovered) {
        renderTagCloud(poseStack, minecraft, x, y, z, aspects, face, scale, alpha, discovered, true);
    }

    public static void renderTagCloud(PoseStack poseStack, Minecraft minecraft, double x, double y, double z, AspectList aspects, @Nullable Direction face, float scale, float alpha, Predicate<Holder<IAspect>> discovered, boolean showAmounts) {
        if (aspects.isEmpty() || scale <= 0.0F) {
            return;
        }
        float tagSize = scale * TAG_SIZE_FACTOR;
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Vec3 anchor = cloudAnchor(x, y, z, face, tagSize);
        float yaw = (float) Mth.atan2(camera.x - anchor.x, camera.z - anchor.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        List<AspectInstance> entries = aspects.sortedByTag();
        poseStack.pushPose();
        poseStack.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
        poseStack.mulPose(Axis.YP.rotation(yaw));
        for (int i = 0; i < entries.size(); i++) {
            AspectInstance entry = entries.get(i);
            int row = i / ROW_SIZE;
            int inRow = Math.min(ROW_SIZE, entries.size() - row * ROW_SIZE);
            float tagX = rowSlide(i % ROW_SIZE, inRow) * tagSize * SPACING_FACTOR;
            float tagY = row * tagSize * ROW_HEIGHT_FACTOR;
            Holder<IAspect> aspect = entry.aspect();
            if (!aspect.isBound()) {
                continue;
            }
            drawTag(poseStack, buffers, aspect, discovered.test(aspect), tagX, tagY, tagSize, alpha);
            if (showAmounts) {
                drawAmount(poseStack, buffers, minecraft.font, entry.amount(), tagX, tagY, tagSize);
            }
        }
        poseStack.popPose();
        buffers.endBatch();
    }

    private static float rowSlide(int column, int inRow) {
        return column - (inRow - 1) * HALF;
    }

    private static Vec3 cloudAnchor(double x, double y, double z, @Nullable Direction face, float tagSize) {
        Vec3 centre = new Vec3(x + HALF, y + HALF, z + HALF);
        if (face == null) {
            return centre;
        }
        double reach = tagSize * FACE_REACH_FACTOR;
        return centre.add(face.getStepX() * reach, face.getStepY() * reach, face.getStepZ() * reach);
    }

    private static void drawTag(PoseStack poseStack, MultiBufferSource buffers, Holder<IAspect> aspect, boolean known, float tagX, float tagY, float tagSize, float alpha) {
        Identifier texture = known ? aspect.value().texture() : UNKNOWN_TEXTURE;
        int color = known ? AspectTagRenderer.colorOf(aspect.value(), alpha, false) : AspectTagRenderer.colorOf(aspect.value(), UNKNOWN_ALPHA, false);
        poseStack.pushPose();
        poseStack.translate(tagX, tagY, 0.0F);
        poseStack.scale(tagSize, tagSize, tagSize);
        renderQuad(poseStack.last(), buffers.getBuffer(billboardType(texture, BlendMode.ALPHA)), color, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void drawAmount(PoseStack poseStack, MultiBufferSource buffers, Font font, int amount, float tagX, float tagY, float tagSize) {
        String digits = Integer.toString(amount);
        float textX = AMOUNT_PEN_OVERHANG - font.width(digits);
        float textY = AMOUNT_TEXT_TOP;
        drawDigits(poseStack, buffers, font, digits, tagX + AMOUNT_RIGHT * tagSize, tagY + AMOUNT_BOTTOM * tagSize, tagSize, DARK_LIFT, textX + SHADOW_STEP, textY + SHADOW_STEP, AMOUNT_SHADOW_COLOR);
        drawDigits(poseStack, buffers, font, digits, tagX + AMOUNT_RIGHT * tagSize, tagY + AMOUNT_BOTTOM * tagSize, tagSize, WHITE_LIFT, textX, textY, AMOUNT_COLOR);
    }

    private static void drawDigits(PoseStack poseStack, MultiBufferSource buffers, Font font, String digits, float cornerX, float cornerY, float tagSize, float lift, float textX, float textY, int color) {
        float pixel = tagSize * TEXT_PIXEL;
        poseStack.pushPose();
        poseStack.translate(cornerX, cornerY, lift);
        poseStack.scale(pixel, -pixel, -pixel);
        font.drawInBatch(digits, textX, textY, color, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, NO_BACKGROUND, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static RenderType billboardType(Identifier texture, BlendMode blend) {
        return switch (blend) {
            case ADDITIVE -> TTFlatRenderTypes.entityAdditiveFlat(texture);
            case ALPHA -> TTFlatRenderTypes.entityTranslucentFlat(texture);
        };
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

    private static void quadVertex(PoseStack.Pose pose, VertexConsumer buffer, float[] corner, int color, int light) {
        buffer.addVertex(pose, corner[0], corner[1], 0.0F).setUv(corner[2], corner[3]).setColor(color).setNormal(pose, 0.0F, 0.0F, NORMAL_Z).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
    }
}
