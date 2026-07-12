package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.api.nodes.NodeModifier;
import com.leclowndu93150.thaumcraft.api.nodes.NodeType;
import com.leclowndu93150.thaumcraft.client.fx.render.FloatyLineRenderer;
import com.leclowndu93150.thaumcraft.client.fx.render.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumcraft.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumcraft.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumcraft.content.item.ThaumometerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class NodeRenderer implements BlockEntityRenderer<BlockEntityNode, NodeRenderState> {
    private static final Identifier NODES_TEXTURE = TCIds.rl("textures/misc/nodes.png");

    private static final RenderType NODE_ADDITIVE = RenderType.create("tc_node_additive",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE)
                    .withTexture("Sampler0", NODES_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());
    private static final RenderType NODE_ADDITIVE_NO_DEPTH = RenderType.create("tc_node_additive_no_depth",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE_NO_DEPTH)
                    .withTexture("Sampler0", NODES_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());
    private static final RenderType NODE_TRANSLUCENT = RenderType.create("tc_node_translucent",
            RenderSetup.builder(TCRenderPipelines.FX_TRANSLUCENT)
                    .withTexture("Sampler0", NODES_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());
    private static final RenderType NODE_TRANSLUCENT_NO_DEPTH = RenderType.create("tc_node_translucent_no_depth",
            RenderSetup.builder(TCRenderPipelines.FX_TRANSLUCENT_NO_DEPTH)
                    .withTexture("Sampler0", NODES_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());

    private static final int GRID = 32;
    private static final double VIEW_DISTANCE = 64.0;
    private static final double THAUMOMETER_VIEW_DISTANCE = 48.0;
    private static final float BASE_LAYER_SCALE = 0.25F;
    private static final float FAINT_ALPHA = 0.1F;
    private static final float FAINT_SCALE = 0.5F;
    private static final float JARRED_SIZE = 0.7F;
    private static final int STRIP_ASPECT = 0;
    private static final int STRIP_NORMAL = 1;
    private static final int STRIP_DARK = 2;
    private static final int STRIP_HUNGRY = 3;
    private static final int STRIP_PURE = 4;
    private static final int STRIP_TAINTED = 5;
    private static final int STRIP_UNSTABLE = 6;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float FRAME_ADVANCE_PER_TICK = 1.25F;
    private static final float ROTATION_PERIOD_BASE = 500.0F;
    private static final float ROTATION_PERIOD_STEP = 50.0F;
    private static final float WHITE_ALPHA_CLAMP = 1.0F;
    private static final float DRAIN_LINE_SPEED = -0.02F;
    private static final float DRAIN_LINE_WIDTH = 0.15F;

    public NodeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public NodeRenderState createRenderState() {
        return new NodeRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityNode node, NodeRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(node, state, partialTicks, cameraPosition, breakProgress);
        state.layers.clear();
        state.type = node.getNodeType();
        state.modifier = node.getNodeModifier();
        state.visible = false;
        state.depthIgnore = false;
        state.alpha = 0.0F;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(node.getBlockPos());
        double distance = Math.sqrt(player.distanceToSqr(center));
        double viewDistance = VIEW_DISTANCE;
        state.size = 1.0F;
        if (node instanceof BlockEntityJarNode) {
            state.visible = true;
            state.size = JARRED_SIZE;
        } else if (GogglesAccess.revealsNodes(player)) {
            state.visible = true;
            state.depthIgnore = true;
        } else if (player.getMainHandItem().getItem() instanceof ThaumometerItem
                || player.getOffhandItem().getItem() instanceof ThaumometerItem) {
            state.visible = true;
            state.depthIgnore = true;
            viewDistance = THAUMOMETER_VIEW_DISTANCE;
        }
        if (distance > viewDistance) {
            state.visible = false;
        }
        float alpha = (float) ((viewDistance - distance) / viewDistance);
        if (state.modifier != null) {
            alpha = switch (state.modifier) {
                case BRIGHT -> alpha * 1.5F;
                case PALE -> alpha * 0.66F;
                case FADING -> alpha * (Mth.sin((player.tickCount + partialTicks) / 3.0F) * 0.25F + 0.33F);
            };
        }
        state.alpha = Math.min(alpha, WHITE_ALPHA_CLAMP);
        state.ticks = player.tickCount + partialTicks;
        state.time = player.level().getGameTime();
        state.frameSeed = node.getBlockPos().getX();
        state.draining = false;
        if (node.getDrainPlayer() != null && player.level().getPlayerByUUID(node.getDrainPlayer())
                instanceof Player drainer && drainer.isUsingItem()) {
            float useTicks = drainer.getTicksUsingItem() + partialTicks;
            float wave = Mth.sin(useTicks / 10.0F) * 10.0F;
            float pitch = Mth.lerp(partialTicks, drainer.xRotO, drainer.getXRot());
            float yaw = Mth.lerp(partialTicks, drainer.yRotO, drainer.getYRot());
            Vec3 offset = new Vec3(-0.1, -0.1, 0.5)
                    .xRot(-pitch * Mth.DEG_TO_RAD)
                    .yRot(-yaw * Mth.DEG_TO_RAD)
                    .yRot(-wave * 0.01F)
                    .xRot(-wave * 0.015F);
            Vec3 hand = new Vec3(
                    Mth.lerp(partialTicks, drainer.xo, drainer.getX()),
                    Mth.lerp(partialTicks, drainer.yo, drainer.getY()) + drainer.getEyeHeight(),
                    Mth.lerp(partialTicks, drainer.zo, drainer.getZ())).add(offset);
            Vec3 nodeCenter = Vec3.atCenterOf(node.getBlockPos());
            state.draining = true;
            state.drainFromX = hand.x - nodeCenter.x;
            state.drainFromY = hand.y - nodeCenter.y;
            state.drainFromZ = hand.z - nodeCenter.z;
            state.drainTime = Math.min(drainer.getTicksUsingItem() + partialTicks, 10.0F) / 10.0F;
            node.clientDrainRed = (((node.getDrainColor() >> 16) & 0xFF) + node.clientDrainRed * 4) / 5;
            node.clientDrainGreen = (((node.getDrainColor() >> 8) & 0xFF) + node.clientDrainGreen * 4) / 5;
            node.clientDrainBlue = ((node.getDrainColor() & 0xFF) + node.clientDrainBlue * 4) / 5;
            state.drainColor = (node.clientDrainRed << 16) | (node.clientDrainGreen << 8) | node.clientDrainBlue;
        }
        for (AspectInstance entry : node.getAspects().entries()) {
            NodeRenderState.AspectLayer layer = new NodeRenderState.AspectLayer();
            layer.color = entry.aspect().value().color();
            layer.amount = entry.amount();
            state.layers.add(layer);
        }
    }

    @Override
    public void submit(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int frame = (int) ((state.ticks * FRAME_ADVANCE_PER_TICK + state.frameSeed) % GRID + GRID) % GRID;
        if (state.draining) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            FloatyLineRenderer.submit(poseStack, collector,
                    new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ),
                    FloatyLineRenderer.time(state.time, state.ticks % 1.0F),
                    state.drainColor, DRAIN_LINE_SPEED, state.drainTime, DRAIN_LINE_WIDTH);
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(camera.orientation);
        if (!state.visible || state.layers.isEmpty()) {
            submitLayer(poseStack, collector, NODE_ADDITIVE, 0.0F, FAINT_SCALE, FAINT_ALPHA, 0xFFFFFF,
                    STRIP_NORMAL, frame);
            poseStack.popPose();
            return;
        }
        float average = 0.0F;
        int count = 0;
        float layerAlpha = state.alpha / Math.max(1.0F, state.layers.size() / 2.0F);
        RenderType aspectType = state.depthIgnore ? NODE_ADDITIVE_NO_DEPTH : NODE_ADDITIVE;
        for (NodeRenderState.AspectLayer layer : state.layers) {
            average += layer.amount;
            float scale = Mth.sin(state.ticks / (14.0F - count)) * BASE_LAYER_SCALE + BASE_LAYER_SCALE * 2.0F;
            scale = (0.2F + scale * (layer.amount / 50.0F)) * state.size;
            float period = (ROTATION_PERIOD_BASE + ROTATION_PERIOD_STEP * count) / 10.0F;
            float angle = (state.time % (long) (period * 20)) / (period * 20.0F) * Mth.TWO_PI;
            submitLayer(poseStack, collector, aspectType, angle, scale, layerAlpha, layer.color,
                    STRIP_ASPECT, frame);
            count++;
        }
        average /= state.layers.size();
        float coreScale = (0.1F + average / 150.0F) * state.size;
        float coreAngle = Mth.TWO_PI * ((state.time % 100L) / 100.0F);
        int strip = switch (state.type) {
            case NORMAL -> STRIP_NORMAL;
            case UNSTABLE -> STRIP_UNSTABLE;
            case DARK -> STRIP_DARK;
            case TAINTED -> STRIP_TAINTED;
            case PURE -> STRIP_PURE;
            case HUNGRY -> STRIP_HUNGRY;
        };
        if (state.type == NodeType.HUNGRY) {
            coreScale *= 0.75F;
        }
        if (state.type == NodeType.UNSTABLE) {
            coreAngle = 0.0F;
        }
        boolean translucentCore = state.type == NodeType.DARK || state.type == NodeType.TAINTED;
        RenderType coreType = translucentCore
                ? (state.depthIgnore ? NODE_TRANSLUCENT_NO_DEPTH : NODE_TRANSLUCENT)
                : aspectType;
        submitLayer(poseStack, collector, coreType, coreAngle, coreScale, state.alpha, 0xFFFFFF, strip, frame);
        poseStack.popPose();
    }

    private static void submitLayer(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType,
                                    float angle, float scale, float alpha, int color, int strip, int frame) {
        float u0 = frame / (float) GRID;
        float u1 = (frame + 1) / (float) GRID;
        float v0 = strip / (float) GRID;
        float v1 = (strip + 1) / (float) GRID;
        int tint = ARGB.color((int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F), color);
        poseStack.pushPose();
        if (angle != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotation(angle));
        }
        float half = scale;
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            buffer.addVertex(mat, -half, -half, 0.0F).setUv(u1, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, -half, half, 0.0F).setUv(u1, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, half, half, 0.0F).setUv(u0, v0).setColor(tint).setLight(EMISSIVE_LIGHT);
            buffer.addVertex(mat, half, -half, 0.0F).setUv(u0, v1).setColor(tint).setLight(EMISSIVE_LIGHT);
        });
        poseStack.popPose();
    }
}
