package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
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
    private static final Identifier TABLE_TEXTURE = TTIds.rl("textures/entity/restable.png");
    private static final Identifier SCROLL_TEXTURE = TTIds.rl("textures/entity/restable2.png");
    private static final Identifier QUILL_TEXTURE = TTIds.rl("textures/entity/tablequill.png");
    private static final Identifier PARCHMENT_TEXTURE = TTIds.rl("textures/misc/parchment.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OPAQUE = 0xFF000000;
    private static final int DEFAULT_NOTE_COLOR = 0x999999;
    private static final float CENTER = 0.5F;
    private static final float FRAME_HEIGHT = 1.0F;
    private static final float HALF_TURN = 180.0F;
    private static final float YAW_NORTH = 270.0F;
    private static final float YAW_SOUTH = 90.0F;
    private static final float YAW_WEST = 180.0F;
    private static final float BOUNDS_MARGIN = 0.5F;
    private static final float QUILL_X = -0.23F;
    private static final float QUILL_Y = -0.095F;
    private static final float QUILL_Z = 0.15F;
    private static final float QUILL_YAW = -105.0F;
    private static final float QUILL_SCALE = 0.5F;
    private static final float QUILL_THICKNESS = 0.025F;
    private static final int QUILL_TEXELS = 16;
    private static final float SHEET_X = 0.1F;
    private static final float SHEET_Z = 0.35F;
    private static final float SHEET_WIDTH = 0.5F;
    private static final float SHEET_LENGTH = 0.6F;
    private static final float SHEET_BASE_HEIGHT = 1.010F;
    private static final float SHEET_STEP = 0.015F;
    private static final float[] SHEET_TILTS = {15.0F, 17.0F, 19.0F, 15.0F, 17.0F, 19.0F};
    private static final float LAY_FLAT = -90.0F;
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
        poseStack.translate(CENTER, FRAME_HEIGHT, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw(state.facing)));
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        if (state.hasTools) {
            collector.submitModelPart(model.inkwell, poseStack, RenderTypes.entityCutout(TABLE_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
            submitQuill(poseStack, collector, light);
        }
        for (int sheet = 0; sheet < SHEET_TILTS.length; sheet++) {
            submitSheet(poseStack, collector, SHEET_BASE_HEIGHT + sheet * SHEET_STEP, SHEET_TILTS[sheet], light);
        }
        if (state.hasNote) {
            collector.submitModelPart(model.scrollTube, poseStack, RenderTypes.entityCutout(SCROLL_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
            poseStack.pushPose();
            poseStack.scale(RIBBON_SCALE, RIBBON_SCALE, RIBBON_SCALE);
            collector.submitModelPart(model.scrollRibbon, poseStack, RenderTypes.entityCutout(SCROLL_TEXTURE), light, OverlayTexture.NO_OVERLAY, null, OPAQUE | state.noteColor, null);
            poseStack.popPose();
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

    private static void submitSheet(PoseStack poseStack, SubmitNodeCollector collector, float heightAboveBase, float tilt, int light) {
        poseStack.pushPose();
        poseStack.translate(SHEET_X, FRAME_HEIGHT - heightAboveBase, SHEET_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(tilt));
        poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT));
        poseStack.scale(SHEET_WIDTH, SHEET_LENGTH, 1.0F);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(PARCHMENT_TEXTURE), (pose, buffer) -> sheet(pose, buffer, light));
        poseStack.popPose();
    }

    private static void sheet(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        vertex(pose, buffer, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
    }

    private static void quill(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        float back = -QUILL_THICKNESS;
        vertex(pose, buffer, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        vertex(pose, buffer, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, light);
        vertex(pose, buffer, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        vertex(pose, buffer, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, light);
        vertex(pose, buffer, 0.0F, 1.0F, back, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 1.0F, 1.0F, back, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 1.0F, 0.0F, back, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 0.0F, 0.0F, back, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        for (int texel = 0; texel < QUILL_TEXELS; texel++) {
            columnWalls(pose, buffer, texel, back, light);
            rowWalls(pose, buffer, texel, back, light);
        }
    }

    private static void columnWalls(PoseStack.Pose pose, VertexConsumer buffer, int column, float back, int light) {
        float left = (float) column / QUILL_TEXELS;
        float right = (float) (column + 1) / QUILL_TEXELS;
        float u = 1.0F - left - 0.5F / QUILL_TEXELS;
        vertex(pose, buffer, left, 0.0F, back, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, left, 0.0F, 0.0F, u, 1.0F, -1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, left, 1.0F, 0.0F, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, left, 1.0F, back, u, 0.0F, -1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, right, 1.0F, back, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, right, 1.0F, 0.0F, u, 0.0F, 1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, right, 0.0F, 0.0F, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, right, 0.0F, back, u, 1.0F, 1.0F, 0.0F, 0.0F, light);
    }

    private static void rowWalls(PoseStack.Pose pose, VertexConsumer buffer, int row, float back, int light) {
        float bottom = (float) row / QUILL_TEXELS;
        float top = (float) (row + 1) / QUILL_TEXELS;
        float v = 1.0F - bottom - 0.5F / QUILL_TEXELS;
        vertex(pose, buffer, 0.0F, top, 0.0F, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
        vertex(pose, buffer, 1.0F, top, 0.0F, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
        vertex(pose, buffer, 1.0F, top, back, 0.0F, v, 0.0F, 1.0F, 0.0F, light);
        vertex(pose, buffer, 0.0F, top, back, 1.0F, v, 0.0F, 1.0F, 0.0F, light);
        vertex(pose, buffer, 1.0F, bottom, 0.0F, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
        vertex(pose, buffer, 0.0F, bottom, 0.0F, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
        vertex(pose, buffer, 0.0F, bottom, back, 1.0F, v, 0.0F, -1.0F, 0.0F, light);
        vertex(pose, buffer, 1.0F, bottom, back, 0.0F, v, 0.0F, -1.0F, 0.0F, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        buffer.addVertex(pose, x, y, z).setColor(WHITE).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    private static Direction facingOf(BlockState state) {
        return state.hasProperty(BlockResearchTable.FACING) ? state.getValue(BlockResearchTable.FACING) : Direction.NORTH;
    }

    private static float yaw(Direction facing) {
        return switch (facing) {
            case NORTH -> YAW_NORTH;
            case SOUTH -> YAW_SOUTH;
            case WEST -> YAW_WEST;
            default -> 0.0F;
        };
    }
}
