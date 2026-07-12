package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.golem.GolemMeshes;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshModel;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshPart;
import com.leclowndu93150.thaumcraft.content.aura.node.BlockEntityNodeStabilizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class NodeStabilizerRenderer
        implements BlockEntityRenderer<BlockEntityNodeStabilizer, NodeStabilizerRenderState> {
    private static final Identifier MODEL = TCIds.rl("models/obj/node_stabilizer.obj");
    private static final Identifier TEXTURE = TCIds.rl("textures/block/node_stabilizer.png");
    private static final Identifier OVERLAY_TEXTURE = TCIds.rl("textures/block/node_stabilizer_over.png");

    private static final RenderType BASE = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType OVERLAY = RenderTypes.entityTranslucentEmissive(OVERLAY_TEXTURE);

    private static final String PART_LOCK = "lock";
    private static final String PART_PISTON = "piston";
    private static final int ARM_COUNT = 4;
    private static final float ARM_ANGLE_STEP = 90.0F;
    private static final float ARM_TWIST = 45.0F;
    private static final float EXTEND_DIVISOR = 100.0F;
    private static final int ADVANCED_TINT = 0xFFFF3333;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OVERLAY_LIGHT_BASE = 50;
    private static final int OVERLAY_LIGHT_RANGE = 170;

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public NodeStabilizerRenderState createRenderState() {
        return new NodeStabilizerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityNodeStabilizer stabilizer, NodeStabilizerRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(stabilizer, state, partialTicks, cameraPosition, breakProgress);
        state.count = stabilizer.count;
        state.advanced = stabilizer.isAdvanced();
        LocalPlayer player = Minecraft.getInstance().player;
        state.ticks = player == null ? 0.0F : player.tickCount + partialTicks;
        state.light = state.lightCoords;
    }

    @Override
    public void submit(NodeStabilizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        submitParts(state.count, state.advanced, state.ticks, poseStack, collector, state.light);
        poseStack.popPose();
    }

    public static void submitParts(int count, boolean advanced, float ticks,
                                   PoseStack poseStack, SubmitNodeCollector collector, int light) {
        MeshModel mesh = GolemMeshes.get(MODEL);
        MeshPart lock = findPart(mesh, PART_LOCK);
        MeshPart piston = findPart(mesh, PART_PISTON);
        if (lock != null) {
            PoseStack.Pose lockPose = poseStack.last().copy();
            collector.submitCustomGeometry(poseStack, BASE, (pose, buffer) ->
                    GolemMeshes.renderPart(mesh, lock, lockPose, buffer, light, WHITE));
        }
        if (piston != null) {
            for (int arm = 0; arm < ARM_COUNT; arm++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(ARM_ANGLE_STEP * arm));
                poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TWIST));
                poseStack.translate(0.0F, 0.0F, count / EXTEND_DIVISOR);
                PoseStack.Pose armPose = poseStack.last().copy();
                collector.submitCustomGeometry(poseStack, BASE, (pose, buffer) ->
                        GolemMeshes.renderPart(mesh, piston, armPose, buffer, light, WHITE));
                float pulse = Mth.sin((ticks + arm * 5) / 3.0F) * 0.1F + 0.9F;
                int glow = OVERLAY_LIGHT_BASE
                        + (int) (OVERLAY_LIGHT_RANGE * (count / (float) BlockEntityNodeStabilizer.MAX_COUNT * pulse));
                int glowUnit = Mth.clamp(glow / 16, 0, 15);
                int glowLight = (glowUnit << 4) | (glowUnit << 20);
                int tint = advanced ? ADVANCED_TINT : WHITE;
                collector.submitCustomGeometry(poseStack, OVERLAY, (pose, buffer) ->
                        GolemMeshes.renderPart(mesh, piston, armPose, buffer, glowLight, tint));
                poseStack.popPose();
            }
        }
    }

    private static @Nullable MeshPart findPart(MeshModel mesh, String name) {
        for (MeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                return part;
            }
        }
        return null;
    }
}
