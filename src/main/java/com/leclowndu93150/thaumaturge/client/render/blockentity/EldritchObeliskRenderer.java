package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchObeliskRenderer implements BlockEntityRenderer<BlockEntityEldritchObelisk, EldritchObeliskRenderState> {
    public static final Identifier CAP_MODEL = TTIds.rl("models/mesh/obelisk_cap.ttmesh");
    public static final String CAP_PART = "Cap";

    private static final Identifier SIDE_TEXTURE = TTIds.rl("textures/entity/obelisk_side.png");
    private static final Identifier SIDE_TEXTURE_OUTER = TTIds.rl("textures/entity/obelisk_side_2.png");
    private static final float BOB_PERIOD = 10.0F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float BASE_HEIGHT = 1.0F;
    private static final float COLUMN_HEIGHT = 3.0F;
    private static final float PLANE_INSET = 0.01F;
    private static final float[] PLANE_OFFSETS = {PLANE_INSET, 1.0F - PLANE_INSET};
    private static final float CENTER = 0.5F;
    private static final float QUARTER_TURN_DEGREES = 90.0F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final float BOUNDS_SIDE_MARGIN = 0.5F;
    private static final float BOUNDS_HEIGHT = 6.0F;
    private static final int VIEW_DISTANCE = 64;

    private static final PanelEdge[] PANEL_EDGES = {new PanelEdge(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F), new PanelEdge(0.0F, 1.0F, 0.0F, 0.0F, -1.0F, 0.0F),
            new PanelEdge(1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F), new PanelEdge(1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F)};

    private record PanelEdge(float startX, float startZ, float endX, float endZ, float normalX, float normalZ) {
    }

    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EldritchObeliskRenderState createRenderState() {
        return new EldritchObeliskRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchObelisk obelisk, EldritchObeliskRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(obelisk, state, partialTicks, cameraPosition, breakProgress);
        state.animationTime = animationTime(partialTicks);
        Level level = obelisk.getLevel();
        state.outerLands = level != null && OuterLands.DIMENSION.equals(level.dimension());
    }

    @Override
    public void submit(EldritchObeliskRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float bottom = BASE_HEIGHT + BOB_AMPLITUDE * Mth.sin(state.animationTime / BOB_PERIOD) + BOB_AMPLITUDE;
        float top = bottom + COLUMN_HEIGHT;
        BlockPos pos = state.blockPos;
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> writeSurfacePlanes(pose, buffer, pos, bottom, top));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(state.outerLands ? SIDE_TEXTURE_OUTER : SIDE_TEXTURE), (pose, buffer) -> writeSidePanels(pose, buffer, bottom, top, light));
        RenderType capType = RenderTypes.entityCutout(state.outerLands ? EldritchCapRenderer.CAP_TEXTURE_OUTER : EldritchCapRenderer.CAP_TEXTURE);
        submitEndCap(poseStack, collector, capType, light, bottom, Axis.XP);
        submitEndCap(poseStack, collector, capType, light, top, Axis.XN);
    }

    private static void submitEndCap(PoseStack poseStack, SubmitNodeCollector collector, RenderType capType, int light, float height, Axis tilt) {
        poseStack.pushPose();
        poseStack.translate(CENTER, height, CENTER);
        poseStack.mulPose(tilt.rotationDegrees(QUARTER_TURN_DEGREES));
        submitCap(CAP_MODEL, poseStack, collector, capType, light);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchObelisk obelisk) {
        BlockPos pos = obelisk.getBlockPos();
        return new AABB(pos.getX() - BOUNDS_SIDE_MARGIN, pos.getY(), pos.getZ() - BOUNDS_SIDE_MARGIN, pos.getX() + 1.0 + BOUNDS_SIDE_MARGIN, pos.getY() + BOUNDS_HEIGHT,
                pos.getZ() + 1.0 + BOUNDS_SIDE_MARGIN);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    static void submitCap(Identifier meshId, PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, int light) {
        for (TTMeshPart part : GolemMeshes.get(meshId).parts()) {
            if (CAP_PART.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, WHITE));
            }
        }
    }

    static float animationTime(float partialTicks) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        return camera == null ? partialTicks : camera.tickCount + partialTicks;
    }

    private static void writeSurfacePlanes(PoseStack.Pose pose, VertexConsumer buffer, BlockPos pos, float bottom, float top) {
        for (float z : PLANE_OFFSETS) {
            EldritchPortalSurface.quad(pose, buffer, pos, 0.0F, bottom, z, 0.0F, top, z, 1.0F, top, z, 1.0F, bottom, z);
        }
        for (float x : PLANE_OFFSETS) {
            EldritchPortalSurface.quad(pose, buffer, pos, x, bottom, 0.0F, x, top, 0.0F, x, top, 1.0F, x, bottom, 1.0F);
        }
    }

    private static void writeSidePanels(PoseStack.Pose pose, VertexConsumer buffer, float bottom, float top, int light) {
        for (PanelEdge edge : PANEL_EDGES) {
            panel(pose, buffer, edge.startX(), edge.startZ(), edge.endX(), edge.endZ(), bottom, top, edge.normalX(), 0.0F, edge.normalZ(), light);
        }
    }

    private static void panel(PoseStack.Pose pose, VertexConsumer buffer, float startX, float startZ, float endX, float endZ, float bottom, float top, float nx, float ny, float nz, int light) {
        sideVertex(pose, buffer, startX, bottom, startZ, 0.0F, 0.0F, nx, ny, nz, light);
        sideVertex(pose, buffer, endX, bottom, endZ, 1.0F, 0.0F, nx, ny, nz, light);
        sideVertex(pose, buffer, endX, top, endZ, 1.0F, 1.0F, nx, ny, nz, light);
        sideVertex(pose, buffer, startX, top, startZ, 0.0F, 1.0F, nx, ny, nz, light);
    }

    private static void sideVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        buffer.addVertex(pose, x, y, z).setColor(WHITE).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
