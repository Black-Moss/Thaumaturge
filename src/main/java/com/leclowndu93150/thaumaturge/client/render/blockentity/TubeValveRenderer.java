package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public final class TubeValveRenderer implements BlockEntityRenderer<BlockEntityTubeValve, TubeValveRenderState> {
    public static final Identifier MODEL_ID = TTIds.rl("block/tube_valve_head");
    public static final StandaloneModelKey<BlockStateModel> MODEL = new StandaloneModelKey<>(MODEL_ID::toString);

    private static final int[] NO_TINTS = new int[0];
    private static final float CENTER = 0.5F;
    private static final float QUARTER_TURN = 90.0F;
    private static final float HALF_TURN = 180.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float SPIN_RATIO = 1.5F;
    private static final float SINK_TRAVEL = 0.11F;
    private static final int OUTLINE_NONE = 0;

    public TubeValveRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public TubeValveRenderState createRenderState() {
        return new TubeValveRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityTubeValve valve, TubeValveRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(valve, state, partialTicks, cameraPosition, breakProgress);
        Direction facing = valve.flowSide();
        state.facing = facing == null ? Direction.UP : facing;
        state.rotation = valve.rotation(partialTicks);
        BlockState blockState = valve.getBlockState();
        state.seed = blockState.getSeed(valve.getBlockPos());
        state.parts.clear();
        state.model = Minecraft.getInstance().getModelManager().getStandaloneModel(MODEL);
        if (state.model != null && valve.getLevel() instanceof ClientLevel level) {
            state.model.collectParts(level, valve.getBlockPos(), blockState, level.getRandom(), state.parts);
        }
    }

    @Override
    public void submit(TubeValveRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.model == null || state.parts.isEmpty()) {
            return;
        }
        float closed = Math.clamp(state.rotation / FULL_TURN, 0.0F, 1.0F);
        poseStack.pushPose();
        poseStack.translate(CENTER, CENTER, CENTER);
        orient(poseStack, state.facing);
        poseStack.translate(0.0F, -closed * SINK_TRAVEL, 0.0F);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotation * SPIN_RATIO));
        poseStack.translate(-CENTER, -CENTER, -CENTER);
        collector.submitMultiLayerBlockModel(poseStack, state.parts, false, NO_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
        if (state.breakProgress != null) {
            collector.submitBreakingBlockModel(poseStack, state.model, state.seed, state.breakProgress.progress());
        }
        poseStack.popPose();
    }

    private static void orient(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(QUARTER_TURN));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(QUARTER_TURN));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(QUARTER_TURN));
            case EAST -> poseStack.mulPose(Axis.ZN.rotationDegrees(QUARTER_TURN));
            default -> {
            }
        }
    }
}
