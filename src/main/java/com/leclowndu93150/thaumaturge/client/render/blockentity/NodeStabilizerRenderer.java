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
import java.util.List;
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
    private static final int STATUS_ATTACHED = 1;
    private static final int STATUS_ENERGIZED = 2;
    private static final int PLAIN_TINT = 0xFFFFFF;
    private static final int ADVANCED_TINT = 0xFF3333;
    private static final int TRANSDUCER_IDLE_TINT = 0x80FF80;
    private static final int TRANSDUCER_ATTACHED_TINT = 0xFF991A;
    private static final int TRANSDUCER_ENERGIZED_TINT = 0xFF004D;
    private static final int BUBBLE_PLAIN_RGB = 0xFFFFFF;
    private static final int BUBBLE_ADVANCED_RGB = 0xFF4444;
    private static final int BUBBLE_LIGHT = 220;
    private static final int OPAQUE_ALPHA = 255;
    private static final float STABILIZER_SLIDE_DIVISOR = 100.0F;
    private static final int TRANSDUCER_COUNT_CAP = 50;
    private static final float TRANSDUCER_EXTENSION_DIVISOR = 137.0F;
    private static final float TRANSDUCER_GLOW_GAIN = 2.5F;
    private static final int GLOW_LIGHT_BASE = 50;
    private static final float GLOW_LIGHT_RANGE = 170.0F;
    private static final float PULSE_BASE = 0.9F;
    private static final float PULSE_AMPLITUDE = 0.1F;
    private static final float PULSE_DIVISOR = 3.0F;
    private static final float PULSE_ARM_SHIFT = 5.0F;
    private static final float BUBBLE_HALF = 0.9F;
    private static final float BUBBLE_HEIGHT = 1.5F;
    private static final float BUBBLE_OPACITY_BASE = 0.5F;
    private static final float BUBBLE_OPACITY_AMPLITUDE = 0.1F;
    private static final float BUBBLE_PULSE_DIVISOR = 8.0F;
    private static final float ALPHA_SCALE = 255.0F;
    private static final int ARM_COUNT = 4;
    private static final int LOCK_ARM = 0;
    private static final float ARM_AZIMUTH_STEP = 90.0F;
    private static final float ARM_TILT = 45.0F;
    private static final float CENTER = 0.5F;
    private static final float STAND_UP = -90.0F;
    private static final List<float[]> BUBBLE_CORNERS = List.of(new float[]{-1.0F, -1.0F, 0.0F, 1.0F}, new float[]{1.0F, -1.0F, 1.0F, 1.0F}, new float[]{1.0F, 1.0F, 1.0F, 0.0F},
            new float[]{-1.0F, 1.0F, 0.0F, 0.0F});
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
        float slide = count / STABILIZER_SLIDE_DIVISOR;
        submitMesh(STABILIZER_SKIN, slide, engagement(count), advanced ? ADVANCED_TINT : PLAIN_TINT, false, ticks, poseStack, collector, light);
    }

    public static void submitTransducerParts(int count, int status, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        float extension = Math.min(count, TRANSDUCER_COUNT_CAP) / TRANSDUCER_EXTENSION_DIVISOR;
        submitMesh(TRANSDUCER_SKIN, extension, extension * TRANSDUCER_GLOW_GAIN, transducerTint(status), true, ticks, poseStack, collector, light);
    }

    private static float engagement(int count) {
        return (float) count / BlockEntityNodeStabilizer.MAX_COUNT;
    }

    private static int transducerTint(int status) {
        return switch (status) {
            case STATUS_ENERGIZED -> TRANSDUCER_ENERGIZED_TINT;
            case STATUS_ATTACHED -> TRANSDUCER_ATTACHED_TINT;
            default -> TRANSDUCER_IDLE_TINT;
        };
    }

    private static void submitMesh(Skin skin, float slide, float glowBase, int tint, boolean lockGlows, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TTMesh mesh = GolemMeshes.get(MESH);
        TTMeshPart lock = findPart(mesh, LOCK_PART);
        TTMeshPart piston = findPart(mesh, PISTON_PART);
        RenderType base = RenderTypes.entityCutout(skin.base());
        RenderType overlay = RenderTypes.entityTranslucent(skin.overlay());
        int overlayColor = ARGB.color(OPAQUE_ALPHA, tint);
        if (lock != null) {
            drawPart(lock, base, WHITE, light, poseStack, collector);
            if (lockGlows) {
                drawPart(lock, overlay, overlayColor, glowLight(glowBase, ticks, LOCK_ARM), poseStack, collector);
            }
        }
        if (piston == null) {
            return;
        }
        for (int arm = 0; arm < ARM_COUNT; arm++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(arm * ARM_AZIMUTH_STEP));
            poseStack.mulPose(Axis.YP.rotationDegrees(ARM_TILT));
            poseStack.translate(0.0F, 0.0F, slide);
            drawPart(piston, base, WHITE, light, poseStack, collector);
            drawPart(piston, overlay, overlayColor, glowLight(glowBase, ticks, arm), poseStack, collector);
            poseStack.popPose();
        }
    }

    private static int glowLight(float glowBase, float ticks, int arm) {
        float pulse = Mth.sin((ticks + arm * PULSE_ARM_SHIFT) / PULSE_DIVISOR) * PULSE_AMPLITUDE + PULSE_BASE;
        return GLOW_LIGHT_BASE + (int) (GLOW_LIGHT_RANGE * glowBase * pulse);
    }

    private static void queueBubble(NodeStabilizerRenderState state, CameraRenderState camera) {
        float alpha = engagement(state.count) * (BUBBLE_OPACITY_BASE + BUBBLE_OPACITY_AMPLITUDE * Mth.sin(state.ticks / BUBBLE_PULSE_DIVISOR));
        int color = ARGB.color((int) (alpha * ALPHA_SCALE), state.advanced ? BUBBLE_ADVANCED_RGB : BUBBLE_PLAIN_RGB);
        Quaternionf facing = new Quaternionf(camera.orientation);
        BlockPos pos = state.blockPos;
        Vec3 origin = new Vec3(pos.getX() + CENTER, pos.getY() + BUBBLE_HEIGHT, pos.getZ() + CENTER);
        LateWorldRenderQueue.enqueue(origin, (late, buffers) -> {
            late.mulPose(facing);
            bubbleQuad(late.last().pose(), buffers.getBuffer(TTFXRenderTypes.additive(BUBBLE_TEXTURE)), color);
        });
    }

    private static void bubbleQuad(Matrix4fc matrix, VertexConsumer buffer, int color) {
        for (float[] corner : BUBBLE_CORNERS) {
            bubbleVertex(matrix, buffer, corner, color);
        }
    }

    private static void bubbleVertex(Matrix4fc matrix, VertexConsumer buffer, float[] corner, int color) {
        buffer.addVertex(matrix, corner[0] * BUBBLE_HALF, corner[1] * BUBBLE_HALF, 0.0F).setUv(corner[2], corner[3]).setColor(color).setLight(BUBBLE_LIGHT);
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

    private record Skin(Identifier base, Identifier overlay) {
    }
}
