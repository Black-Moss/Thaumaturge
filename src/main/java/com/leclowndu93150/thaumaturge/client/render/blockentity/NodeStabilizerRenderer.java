package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeStabilizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class NodeStabilizerRenderer implements BlockEntityRenderer<BlockEntityNodeStabilizer, NodeStabilizerRenderState> {
    private static final Identifier MESH = TTIds.rl("models/mesh/node_stabilizer.ttmesh");
    private static final Skin STABILIZER_SKIN = new Skin(TTIds.rl("textures/block/node_stabilizer.png"), TTIds.rl("textures/block/node_stabilizer_over.png"));
    private static final Skin TRANSDUCER_SKIN = new Skin(TTIds.rl("textures/block/node_converter.png"), TTIds.rl("textures/block/node_converter_over.png"));
    private static final Identifier BUBBLE_TEXTURE = TTIds.rl("textures/misc/node_bubble.png");
    private static final String LOCK_PART = "lock";
    private static final String PISTON_PART = "piston";
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ADVANCED_TINT = 0xFFFF3333;
    private static final int TRANSDUCER_ENERGIZED_TINT = 0xFFFF004D;
    private static final int TRANSDUCER_ATTACHED_TINT = 0xFFFF991A;
    private static final int TRANSDUCER_IDLE_TINT = 0xFF80FF80;
    private static final int STATUS_ATTACHED = 1;
    private static final int STATUS_ENERGIZED = 2;
    private static final int BUBBLE_ADVANCED_RGB = 0xFF4444;
    private static final int BUBBLE_PLAIN_RGB = 0xFFFFFF;
    private static final int BUBBLE_LIGHT = 220;
    private static final int ARM_COUNT = 4;
    private static final float ARM_AZIMUTH_STEP = 90.0F;
    private static final float ARM_TILT = 45.0F;
    private static final float ARM_EXTENSION_DIVISOR = 100.0F;
    private static final float TRANSDUCER_EXTENSION_CAP = 50.0F;
    private static final float TRANSDUCER_EXTENSION_DIVISOR = 137.0F;
    private static final float TRANSDUCER_GLOW_GAIN = 2.5F;
    private static final int OVERLAY_LIGHT_BASE = 50;
    private static final float OVERLAY_LIGHT_RANGE = 170.0F;
    private static final float PULSE_AMPLITUDE = 0.1F;
    private static final float PULSE_BASE = 0.9F;
    private static final float PULSE_PERIOD = 3.0F;
    private static final float PULSE_ARM_PHASE = 5.0F;
    private static final float BUBBLE_PULSE_PERIOD = 8.0F;
    private static final float BUBBLE_PULSE_BASE = 0.5F;
    private static final float BUBBLE_HALF = 0.9F;
    private static final float BUBBLE_HEIGHT = 1.5F;
    private static final float CENTER = 0.5F;
    private static final float STAND_UP = -90.0F;
    private static final float BYTE_RANGE = 255.0F;
    private static final double BOUNDS_CENTER_LIFT = 1.0;
    private static final double BOUNDS_SIDE = 2.0 * BUBBLE_HALF * Math.sqrt(2.0);

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public NodeStabilizerRenderState createRenderState() {
        return new NodeStabilizerRenderState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNodeStabilizer stabilizer) {
        BlockPos pos = stabilizer.getBlockPos();
        AABB bubble = AABB.ofSize(Vec3.atCenterOf(pos).add(0.0, BOUNDS_CENTER_LIFT, 0.0), BOUNDS_SIDE, BOUNDS_SIDE, BOUNDS_SIDE);
        return new AABB(pos).minmax(bubble);
    }

    @Override
    public void extractRenderState(BlockEntityNodeStabilizer stabilizer, NodeStabilizerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(stabilizer, state, partialTicks, cameraPosition, breakProgress);
        state.count = stabilizer.count;
        state.advanced = stabilizer.isAdvanced();
        LocalPlayer player = Minecraft.getInstance().player;
        state.ticks = player == null ? 0.0F : player.tickCount + partialTicks;
        state.light = state.lightCoords;
    }

    @Override
    public void submit(NodeStabilizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(STAND_UP));
        submitParts(state.count, state.advanced, state.ticks, poseStack, collector, state.light);
        poseStack.popPose();
        if (state.count > 0) {
            queueBubble(state, camera);
        }
    }

    public static void submitParts(int count, boolean advanced, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submitMesh(STABILIZER_SKIN, count / ARM_EXTENSION_DIVISOR, (float) count / BlockEntityNodeStabilizer.MAX_COUNT, false, advanced ? ADVANCED_TINT : WHITE, ticks, poseStack, collector, light);
    }

    public static void submitTransducerParts(int count, int status, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        float extension = Math.min(TRANSDUCER_EXTENSION_CAP, count) / TRANSDUCER_EXTENSION_DIVISOR;
        int tint = status == STATUS_ENERGIZED ? TRANSDUCER_ENERGIZED_TINT : status == STATUS_ATTACHED ? TRANSDUCER_ATTACHED_TINT : TRANSDUCER_IDLE_TINT;
        submitMesh(TRANSDUCER_SKIN, extension, extension * TRANSDUCER_GLOW_GAIN, true, tint, ticks, poseStack, collector, light);
    }

    private static void submitMesh(Skin skin, float extension, float glow, boolean glowingLock, int tint, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TTMesh mesh = GolemMeshes.get(MESH);
        TTMeshPart lock = findPart(mesh, LOCK_PART);
        TTMeshPart piston = findPart(mesh, PISTON_PART);
        RenderType base = RenderTypes.entityCutout(skin.base());
        RenderType overlay = RenderTypes.entityTranslucent(skin.overlay());
        if (lock != null) {
            drawPart(lock, base, WHITE, light, poseStack, collector);
            if (glowingLock) {
                drawPart(lock, overlay, tint, overlayLight(glow, ticks, 0), poseStack, collector);
            }
        }
        if (piston == null) {
            return;
        }
        for (int arm = 0; arm < ARM_COUNT; arm++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(arm * ARM_AZIMUTH_STEP));
            poseStack.mulPose(Axis.XP.rotationDegrees(ARM_TILT));
            poseStack.translate(0.0F, 0.0F, extension);
            drawPart(piston, base, WHITE, light, poseStack, collector);
            drawPart(piston, overlay, tint, overlayLight(glow, ticks, arm), poseStack, collector);
            poseStack.popPose();
        }
    }

    private static int overlayLight(float glow, float ticks, int arm) {
        float pulse = PULSE_AMPLITUDE * Mth.sin((ticks + PULSE_ARM_PHASE * arm) / PULSE_PERIOD) + PULSE_BASE;
        return OVERLAY_LIGHT_BASE + (int) (OVERLAY_LIGHT_RANGE * (glow * pulse));
    }

    private static void drawPart(TTMeshPart part, RenderType type, int color, int light, PoseStack poseStack, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, color));
    }

    private static @Nullable TTMeshPart findPart(TTMesh mesh, String name) {
        for (TTMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                return part;
            }
        }
        return null;
    }

    private static void queueBubble(NodeStabilizerRenderState state, CameraRenderState camera) {
        float pulse = PULSE_AMPLITUDE * Mth.sin(state.ticks / BUBBLE_PULSE_PERIOD) + BUBBLE_PULSE_BASE;
        float alpha = (float) state.count / BlockEntityNodeStabilizer.MAX_COUNT * pulse;
        int color = ARGB.color((int) (alpha * BYTE_RANGE), state.advanced ? BUBBLE_ADVANCED_RGB : BUBBLE_PLAIN_RGB);
        Quaternionf orientation = new Quaternionf(camera.orientation);
        Vec3 origin = Vec3.atBottomCenterOf(state.blockPos).add(0.0, BUBBLE_HEIGHT, 0.0);
        LateWorldRenderQueue.enqueue(origin, (poseStack, buffers) -> {
            poseStack.pushPose();
            poseStack.mulPose(orientation);
            bubbleQuad(poseStack.last().pose(), buffers.getBuffer(TTFXRenderTypes.additiveSorted(BUBBLE_TEXTURE)), color);
            poseStack.popPose();
        });
    }

    private static void bubbleQuad(Matrix4fc matrix, VertexConsumer buffer, int color) {
        bubbleVertex(matrix, buffer, -BUBBLE_HALF, -BUBBLE_HALF, 0.0F, 1.0F, color);
        bubbleVertex(matrix, buffer, BUBBLE_HALF, -BUBBLE_HALF, 1.0F, 1.0F, color);
        bubbleVertex(matrix, buffer, BUBBLE_HALF, BUBBLE_HALF, 1.0F, 0.0F, color);
        bubbleVertex(matrix, buffer, -BUBBLE_HALF, BUBBLE_HALF, 0.0F, 0.0F, color);
    }

    private static void bubbleVertex(Matrix4fc matrix, VertexConsumer buffer, float x, float y, float u, float v, int color) {
        buffer.addVertex(matrix, x, y, 0.0F).setUv(u, v).setColor(color).setLight(BUBBLE_LIGHT);
    }

    private record Skin(Identifier base, Identifier overlay) {
    }
}
