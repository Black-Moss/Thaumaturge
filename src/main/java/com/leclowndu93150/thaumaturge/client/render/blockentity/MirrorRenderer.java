package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorBase;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public final class MirrorRenderer implements BlockEntityRenderer<BlockEntityMirrorBase, MirrorRenderState> {
    public static final Identifier FRAME_MODEL_ID = TTIds.rl("block/mirror");
    public static final Identifier FRAME_ESSENTIA_MODEL_ID = TTIds.rl("block/mirror_essentia");
    public static final StandaloneModelKey<BlockStateModel> FRAME_MODEL = new StandaloneModelKey<>(FRAME_MODEL_ID::toString);
    public static final StandaloneModelKey<BlockStateModel> FRAME_ESSENTIA_MODEL = new StandaloneModelKey<>(FRAME_ESSENTIA_MODEL_ID::toString);

    private static final Identifier PANE_TEXTURE = TTIds.rl("textures/block/mirrorpane.png");
    private static final Identifier PANE_LINKED_TEXTURE = TTIds.rl("textures/block/mirrorpanetrans.png");
    private static final double RANGE = 32.0;
    private static final double RANGE_SQUARED = RANGE * RANGE;
    private static final float CENTER = 0.5F;
    private static final float FRAME_DROP = 0.46875F;
    private static final float FRAME_TILT = -90.0F;
    private static final float PANE_Y = -0.5F + 0.02F;
    private static final float PORTAL_Y = -0.5F + 0.018F;
    private static final float PANE_HALF = 0.5F;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float YAW_EAST = 90.0F;
    private static final float YAW_SOUTH = 180.0F;
    private static final float YAW_WEST = 270.0F;
    private static final float PITCH_DOWN = 180.0F;
    private static final float PITCH_SIDE = 90.0F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int[] NO_TINTS = new int[0];
    private static final float[][] PANE_CORNERS = {{-PANE_HALF, -PANE_HALF, 0.0F, 0.0F}, {-PANE_HALF, PANE_HALF, 0.0F, 1.0F}, {PANE_HALF, PANE_HALF, 1.0F, 1.0F}, {PANE_HALF, -PANE_HALF, 1.0F, 0.0F}};
    private static final int[][] PORTAL_STRIPS = {{2, 5, 11}, {3, 5, 11}, {4, 4, 12}, {5, 3, 13}, {6, 3, 13}, {7, 3, 13}, {8, 3, 13}, {9, 3, 13}, {10, 3, 13}, {11, 4, 12}, {12, 5, 11}, {13, 5, 11}};

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public MirrorRenderState createRenderState() {
        return new MirrorRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityMirrorBase mirror, MirrorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(mirror, state, partialTicks, cameraPosition, breakProgress);
        BlockState block = mirror.getBlockState();
        state.facing = block.getValue(BlockMirror.FACING);
        state.linked = mirror.paired;
        LocalPlayer player = Minecraft.getInstance().player;
        state.inRange = player != null && player.distanceToSqr(Vec3.atCenterOf(mirror.getBlockPos())) <= RANGE_SQUARED;
        state.frameParts.clear();
        boolean essentia = block.getBlock() instanceof BlockMirror mirrorBlock && mirrorBlock.isEssentia();
        BlockStateModel frame = Minecraft.getInstance().getModelManager().getStandaloneModel(essentia ? FRAME_ESSENTIA_MODEL : FRAME_MODEL);
        if (frame != null && mirror.getLevel() instanceof ClientLevel clientLevel) {
            frame.collectParts(clientLevel, mirror.getBlockPos(), block, clientLevel.getRandom(), state.frameParts);
        }
    }

    @Override
    public void submit(MirrorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(CENTER, CENTER, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw(state.facing)));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch(state.facing)));
        if (!state.frameParts.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -FRAME_DROP, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(FRAME_TILT));
            poseStack.translate(-CENTER, -CENTER, -CENTER);
            collector.submitMultiLayerBlockModel(poseStack, state.frameParts, true, NO_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        boolean portal = state.linked && state.inRange;
        if (portal) {
            BlockPos worldPos = state.blockPos;
            collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> portalWindow(pose, buffer, worldPos));
        }
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(portal ? PANE_LINKED_TEXTURE : PANE_TEXTURE), (pose, buffer) -> pane(pose, buffer, light));
        poseStack.popPose();
    }

    private static void pane(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        for (float[] corner : PANE_CORNERS) {
            JarRenderer.addVertex(buffer, pose, corner[0], PANE_Y, corner[1], corner[2], corner[3], WHITE, light, 0.0F, 1.0F, 0.0F);
        }
    }

    private static void portalWindow(PoseStack.Pose pose, VertexConsumer buffer, BlockPos worldPos) {
        for (int[] strip : PORTAL_STRIPS) {
            float z0 = strip[0] * PIXEL - PANE_HALF;
            float z1 = z0 + PIXEL;
            float x0 = strip[1] * PIXEL - PANE_HALF;
            float x1 = strip[2] * PIXEL - PANE_HALF;
            EldritchPortalSurface.quad(pose, buffer, worldPos, x0, PORTAL_Y, z0, x0, PORTAL_Y, z1, x1, PORTAL_Y, z1, x1, PORTAL_Y, z0);
        }
    }

    private static float yaw(Direction facing) {
        return switch (facing) {
            case EAST -> YAW_EAST;
            case SOUTH -> YAW_SOUTH;
            case WEST -> YAW_WEST;
            default -> 0.0F;
        };
    }

    private static float pitch(Direction facing) {
        return switch (facing) {
            case UP -> 0.0F;
            case DOWN -> PITCH_DOWN;
            default -> PITCH_SIDE;
        };
    }
}
