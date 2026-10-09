package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class NodeTransducerRenderer implements BlockEntityRenderer<BlockEntityNodeTransducer, NodeTransducerRenderState> {
    private static final float ANCHOR_X = 0.5F;
    private static final float ANCHOR_Y = 1.0F;
    private static final float ANCHOR_Z = 0.5F;
    private static final float QUARTER_TURN = 90.0F;

    public NodeTransducerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public NodeTransducerRenderState createRenderState() {
        return new NodeTransducerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityNodeTransducer transducer, NodeTransducerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(transducer, state, partialTicks, cameraPosition, breakProgress);
        state.count = transducer.getCount();
        state.status = transducer.getStatus();
        LocalPlayer player = Minecraft.getInstance().player;
        state.ticks = player == null ? 0.0F : player.tickCount + partialTicks;
        state.light = state.lightCoords;
    }

    @Override
    public void submit(NodeTransducerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(ANCHOR_X, ANCHOR_Y, ANCHOR_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(QUARTER_TURN));
        NodeStabilizerRenderer.submitTransducerParts(state.count, state.status, state.ticks, poseStack, collector, state.light);
        poseStack.popPose();
    }
}
