package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.BlockEntityEldritchPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchPortalRenderer implements BlockEntityRenderer<BlockEntityEldritchPortal, EldritchPortalRenderState> {
    private static final Identifier TEXTURE = TTIds.rl("textures/misc/eldritch_portal.png");
    private static final Identifier TEXTURE_OVERWORLD = TTIds.rl("textures/misc/eldritch_portal_overworld.png");
    private static final int FRAME_COUNT = 32;
    private static final float WIDEN_TICKS = 5.0F;
    private static final float GROW_TICKS = 30.0F;
    private static final float CENTER = 0.5F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int PORTAL_LIGHT = 0x00F000DC;
    private static final float BOUNDS_MARGIN = 1.5F;
    private static final int VIEW_DISTANCE = 64;

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EldritchPortalRenderState createRenderState() {
        return new EldritchPortalRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchPortal portal, EldritchPortalRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(portal, state, partialTicks, cameraPosition, breakProgress);
        state.openCount = portal.openTicks() + partialTicks;
        state.animationTime = EldritchObeliskRenderer.animationTime(partialTicks);
        Level level = portal.getLevel();
        state.towardOverworld = level != null && OuterLands.DIMENSION.equals(level.dimension());
    }

    @Override
    public void submit(EldritchPortalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.openCount < 0.0F) {
            return;
        }
        float halfWidth = Math.min(WIDEN_TICKS, state.openCount) / WIDEN_TICKS;
        float halfHeight = Math.min(GROW_TICKS, state.openCount) / GROW_TICKS;
        int frame = Math.floorMod((int) state.animationTime, FRAME_COUNT);
        float uMin = StripUv.u0(frame, FRAME_COUNT);
        float uMax = StripUv.u1(frame, FRAME_COUNT);
        poseStack.pushPose();
        poseStack.translate(CENTER, CENTER, CENTER);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.translucent(state.towardOverworld ? TEXTURE_OVERWORLD : TEXTURE),
                (pose, buffer) -> writeSprite(pose, buffer, halfWidth, halfHeight, uMin, uMax));
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchPortal portal) {
        return new AABB(portal.getBlockPos()).inflate(BOUNDS_MARGIN);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    private static void writeSprite(PoseStack.Pose pose, VertexConsumer buffer, float halfWidth, float halfHeight, float uMin, float uMax) {
        vertex(pose, buffer, -halfWidth, -halfHeight, uMax, StripUv.V0);
        vertex(pose, buffer, -halfWidth, halfHeight, uMax, StripUv.V1);
        vertex(pose, buffer, halfWidth, halfHeight, uMin, StripUv.V1);
        vertex(pose, buffer, halfWidth, -halfHeight, uMin, StripUv.V0);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F).setUv(u, v).setColor(WHITE).setLight(PORTAL_LIGHT);
    }
}
