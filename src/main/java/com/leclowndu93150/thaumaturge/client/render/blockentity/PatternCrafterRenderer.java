package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockEntityPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockPatternCrafter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PatternCrafterRenderer implements BlockEntityRenderer<BlockEntityPatternCrafter, PatternCrafterRenderState> {
    private static final Identifier MODES_TEXTURE = TTIds.rl("textures/block/pattern_crafter_modes.png");
    private static final Identifier GEAR_TEXTURE = TTIds.rl("textures/misc/gear_brass.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final float CENTER = 0.5F;
    private static final float YAW_SOUTH = 180.0F;
    private static final float YAW_WEST = 90.0F;
    private static final float YAW_EAST = 270.0F;
    private static final float FULL_TURN = 360.0F;
    private static final int MODE_FRAMES = 10;
    private static final float SYMBOL_PLANE = -0.501F;
    private static final float SYMBOL_HALF_WIDTH = 0.25F;
    private static final float SYMBOL_BOTTOM = 0.5F;
    private static final float SYMBOL_TOP = 1.0F;
    private static final float GEAR_PLANE = -0.505F;
    private static final float GEAR_OFFSET_X = 0.2F;
    private static final float GEAR_HEIGHT = 0.34375F;
    private static final float GEAR_HALF = 0.25F;

    public PatternCrafterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public PatternCrafterRenderState createRenderState() {
        return new PatternCrafterRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityPatternCrafter crafter, PatternCrafterRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crafter, state, partialTicks, cameraPosition, breakProgress);
        state.facing = crafter.getBlockState().getValue(BlockPatternCrafter.FACING);
        state.patternType = crafter.patternType();
        state.rotation = (crafter.rot + crafter.rotSpeed * partialTicks) % FULL_TURN;
    }

    @Override
    public void submit(PatternCrafterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        float u0 = (float) state.patternType / MODE_FRAMES;
        float u1 = (float) (state.patternType + 1) / MODE_FRAMES;
        poseStack.pushPose();
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw(state.facing)));
        RenderType modes = RenderTypes.entityCutout(MODES_TEXTURE);
        collector.submitCustomGeometry(poseStack, modes, (pose, buffer) -> symbol(pose, buffer, u0, u1, light));
        RenderType gear = RenderTypes.entityCutout(GEAR_TEXTURE);
        submitGear(poseStack, collector, gear, -GEAR_OFFSET_X, -state.rotation, light);
        submitGear(poseStack, collector, gear, GEAR_OFFSET_X, state.rotation, light);
        poseStack.popPose();
    }

    private static void submitGear(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, float x, float angle, int light) {
        poseStack.pushPose();
        poseStack.translate(x, GEAR_HEIGHT, GEAR_PLANE);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> gearQuad(pose, buffer, light));
        poseStack.popPose();
    }

    private static float yaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> YAW_SOUTH;
            case WEST -> YAW_WEST;
            case EAST -> YAW_EAST;
            default -> 0.0F;
        };
    }

    private static void symbol(PoseStack.Pose pose, VertexConsumer buffer, float u0, float u1, int light) {
        vertex(pose, buffer, -SYMBOL_HALF_WIDTH, SYMBOL_BOTTOM, SYMBOL_PLANE, u0, 1.0F, light);
        vertex(pose, buffer, -SYMBOL_HALF_WIDTH, SYMBOL_TOP, SYMBOL_PLANE, u0, 0.0F, light);
        vertex(pose, buffer, SYMBOL_HALF_WIDTH, SYMBOL_TOP, SYMBOL_PLANE, u1, 0.0F, light);
        vertex(pose, buffer, SYMBOL_HALF_WIDTH, SYMBOL_BOTTOM, SYMBOL_PLANE, u1, 1.0F, light);
    }

    private static void gearQuad(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        vertex(pose, buffer, -GEAR_HALF, -GEAR_HALF, 0.0F, 0.0F, 1.0F, light);
        vertex(pose, buffer, -GEAR_HALF, GEAR_HALF, 0.0F, 0.0F, 0.0F, light);
        vertex(pose, buffer, GEAR_HALF, GEAR_HALF, 0.0F, 1.0F, 0.0F, light);
        vertex(pose, buffer, GEAR_HALF, -GEAR_HALF, 0.0F, 1.0F, 1.0F, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int light) {
        buffer.addVertex(pose, x, y, z).setColor(WHITE).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
