package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ResearchTableRenderer implements BlockEntityRenderer<BlockEntityResearchTable, ResearchTableRenderState> {
    private static final Identifier INKWELL_MESH = TTIds.rl("models/mesh/research_inkwell.ttmesh");
    private static final Identifier INKWELL_TEXTURE = TTIds.rl("textures/entity/research_inkwell.png");
    private static final Identifier SCROLL_TEXTURE = TTIds.rl("textures/entity/restable2.png");
    private static final Identifier QUILL_TEXTURE = TTIds.rl("textures/entity/tablequill.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OPAQUE = 0xFF000000;
    private static final int DEFAULT_NOTE_COLOR = 0x999999;
    private static final float HALF_TURN = 180.0F;
    private static final float BOUNDS_MARGIN = 0.5F;
    private static final float INKWELL_X = -0.28125F;
    private static final float INKWELL_Z = 0.28125F;
    private static final float QUILL_X = -0.226F;
    private static final float QUILL_Y = -0.095F;
    private static final float QUILL_Z = 0.076F;
    private static final float QUILL_YAW = -105.0F;
    private static final float QUILL_SCALE = 0.5F;
    private static final float QUILL_THICKNESS = 0.025F;
    private static final int QUILL_TEXELS = 16;
    private static final float RIBBON_SCALE = 1.2F;

    private final ResearchTableModel model;

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ResearchTableModel(context.bakeLayer(TTModelLayers.RESEARCH_TABLE));
    }

    @Override
    public ResearchTableRenderState createRenderState() {
        return new ResearchTableRenderState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityResearchTable table) {
        BlockPos pos = table.getBlockPos();
        return new AABB(pos).minmax(new AABB(pos.relative(facingOf(table.getBlockState()))).inflate(BOUNDS_MARGIN));
    }

    @Override
    public void extractRenderState(BlockEntityResearchTable table, ResearchTableRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(table, state, partialTicks, cameraPosition, breakProgress);
        state.facing = facingOf(table.getBlockState());
        ItemStack tools = table.items().getResource(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS).toStack();
        state.hasTools = tools.is(TTItemTags.SCRIBING_TOOLS);
        ItemStack note = table.items().getResource(BlockEntityResearchTable.SLOT_NOTE).toStack();
        ResearchNoteData data = note.isEmpty() ? null : table.noteData();
        state.hasNote = data != null;
        state.noteColor = data == null ? DEFAULT_NOTE_COLOR : data.color();
    }

    @Override
    public void submit(ResearchTableRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        ResearchTableTop.pose(poseStack, state.facing);
        if (state.hasTools) {
            submitInkwell(poseStack, collector, light);
            submitQuill(poseStack, collector, light);
        }
        ResearchTableTop.submitSheets(poseStack, collector, light);
        if (state.hasNote) {
            collector.submitModelPart(model.scrollTube, poseStack, RenderTypes.entityCutout(SCROLL_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
            poseStack.pushPose();
            poseStack.scale(RIBBON_SCALE, RIBBON_SCALE, RIBBON_SCALE);
            collector.submitModelPart(model.scrollRibbon, poseStack, RenderTypes.entityCutout(SCROLL_TEXTURE), light, OverlayTexture.NO_OVERLAY, null, OPAQUE | state.noteColor, null);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitInkwell(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        poseStack.translate(INKWELL_X, 0.0F, INKWELL_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        for (TTMeshPart part : GolemMeshes.get(INKWELL_MESH).parts()) {
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(INKWELL_TEXTURE), (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, WHITE));
        }
        poseStack.popPose();
    }

    private static void submitQuill(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        poseStack.translate(QUILL_X, QUILL_Y, QUILL_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(QUILL_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        poseStack.scale(QUILL_SCALE, QUILL_SCALE, QUILL_SCALE);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(QUILL_TEXTURE), (pose, buffer) -> quill(pose, buffer, light));
        poseStack.popPose();
    }

    private static void quill(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        float back = -QUILL_THICKNESS;
        ResearchTableTop.vertex(pose, buffer, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, 1.0F, back, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 1.0F, back, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, 0.0F, back, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, 0.0F, back, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        for (int texel = 0; texel < QUILL_TEXELS; texel++) {
            columnWalls(pose, buffer, texel, back, light);
            rowWalls(pose, buffer, texel, back, light);
        }
    }

    private static void columnWalls(PoseStack.Pose pose, VertexConsumer buffer, int column, float back, int light) {
        float left = (float) column / QUILL_TEXELS;
        float right = (float) (column + 1) / QUILL_TEXELS;
        float u = 1.0F - left - 0.5F / QUILL_TEXELS;
        ResearchTableTop.vertex(pose, buffer, left, 0.0F, back, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, left, 0.0F, 0.0F, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, left, 1.0F, 0.0F, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, left, 1.0F, back, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, right, 1.0F, back, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, right, 1.0F, 0.0F, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, right, 0.0F, 0.0F, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, right, 0.0F, back, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
    }

    private static void rowWalls(PoseStack.Pose pose, VertexConsumer buffer, int row, float back, int light) {
        float bottom = (float) row / QUILL_TEXELS;
        float top = (float) (row + 1) / QUILL_TEXELS;
        float v = 1.0F - bottom - 0.5F / QUILL_TEXELS;
        ResearchTableTop.vertex(pose, buffer, 0.0F, top, 0.0F, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, top, 0.0F, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, top, back, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, top, back, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, bottom, 0.0F, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, bottom, 0.0F, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 0.0F, bottom, back, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
        ResearchTableTop.vertex(pose, buffer, 1.0F, bottom, back, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
    }

    private static Direction facingOf(BlockState state) {
        return state.hasProperty(BlockResearchTable.FACING) ? state.getValue(BlockResearchTable.FACING) : Direction.NORTH;
    }
}
