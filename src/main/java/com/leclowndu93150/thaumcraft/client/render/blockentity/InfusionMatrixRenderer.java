package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class InfusionMatrixRenderer implements BlockEntityRenderer<BlockEntityInfusionMatrix, InfusionMatrixRenderState> {
    private static final Identifier TEX_NORMAL = TCIds.rl("textures/block/infuser_normal.png");
    private static final Identifier TEX_ANCIENT = TCIds.rl("textures/block/infuser_ancient.png");
    private static final Identifier TEX_ELDRITCH = TCIds.rl("textures/block/infuser_eldritch.png");

    private static final float SUB_CUBE_OFFSET = 0.25F;
    private static final float SUB_CUBE_SCALE = 0.45F;
    private static final float SUB_CUBE_HALF = 0.5F * SUB_CUBE_SCALE;
    private static final float TILT_X = 35.0F;
    private static final float TILT_Z = 45.0F;
    private static final float JITTER_SCALE = 0.01F;
    private static final float BASE_TEXTURE_V = 0.0F;
    private static final float GLOW_TEXTURE_V = 0.5F;

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public InfusionMatrixRenderState createRenderState() {
        return new InfusionMatrixRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityInfusionMatrix matrix, InfusionMatrixRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(matrix, state, partialTicks, cameraPosition, breakProgress);
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.animationTime = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
        state.startUp = matrix.clientStartUp;
        state.stability = matrix.stability();
        state.craftTicks = matrix.clientCraftTicks;
        state.active = matrix.isActive();
        state.texture = pickTexture(matrix);
    }

    private static Identifier pickTexture(BlockEntityInfusionMatrix matrix) {
        Level level = matrix.getLevel();
        if (level == null) {
            return TEX_NORMAL;
        }
        BlockPos corner = matrix.getBlockPos().offset(-1, -2, -1);
        Block block = level.getBlockState(corner).getBlock();
        if (block == TCBlocks.PILLAR_ANCIENT.get()) {
            return TEX_ANCIENT;
        }
        if (block == TCBlocks.PILLAR_ELDRITCH.get()) {
            return TEX_ELDRITCH;
        }
        return TEX_NORMAL;
    }

    @Override
    public void submit(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderType type = RenderTypes.entityCutout(state.texture);
        float instability = Math.min(6.0F,
                1.0F + (state.stability < 0.0F ? -state.stability * 0.66F : 1.0F) * (Math.min(state.craftTicks, 50) / 50.0F));
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.animationTime % 360.0F * state.startUp));
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT_X * state.startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT_Z * state.startUp));
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float jx = 0.0F;
                    float jy = 0.0F;
                    float jz = 0.0F;
                    if (state.active) {
                        jx = Mth.sin((state.animationTime + a * 10) / 15.0F) * JITTER_SCALE * state.startUp * instability;
                        jy = Mth.sin((state.animationTime + b * 10) / 14.0F) * JITTER_SCALE * state.startUp * instability;
                        jz = Mth.sin((state.animationTime + c * 10) / 13.0F) * JITTER_SCALE * state.startUp * instability;
                    }
                    int aa = a == 0 ? -1 : 1;
                    int bb = b == 0 ? -1 : 1;
                    int cc = c == 0 ? -1 : 1;
                    poseStack.pushPose();
                    poseStack.translate(jx + aa * SUB_CUBE_OFFSET, jy + bb * SUB_CUBE_OFFSET, jz + cc * SUB_CUBE_OFFSET);
                    if (a > 0) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    }
                    if (b > 0) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    }
                    if (c > 0) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    }
                    int light = state.lightCoords;
                    collector.submitCustomGeometry(poseStack, type,
                            (pose, buffer) -> drawCube(buffer, pose.pose(), light, BASE_TEXTURE_V));
                    poseStack.popPose();
                }
            }
        }
        poseStack.popPose();
    }

    private static void drawCube(VertexConsumer buffer, Matrix4fc pose, int light, float vOffset) {
        for (Direction face : Direction.values()) {
            drawFace(buffer, pose, face, light, vOffset);
        }
    }

    private static void drawFace(VertexConsumer buffer, Matrix4fc pose, Direction face, int light, float vOffset) {
        float h = SUB_CUBE_HALF;
        Vec3 normal = Vec3.atLowerCornerOf(face.getUnitVec3i());
        Vec3 up = Math.abs(normal.y) > 0.5 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = normal.cross(up);
        Vec3 center = normal.scale(h);
        Vec3[] corners = new Vec3[]{
                center.subtract(right.scale(h)).subtract(up.scale(h)),
                center.subtract(right.scale(h)).add(up.scale(h)),
                center.add(right.scale(h)).add(up.scale(h)),
                center.add(right.scale(h)).subtract(up.scale(h))
        };
        float[][] uvs = {{0.0F, 0.0F}, {0.0F, 0.5F}, {1.0F, 0.5F}, {1.0F, 0.0F}};
        for (int i = 0; i < 4; i++) {
            Vec3 v = corners[i];
            buffer.addVertex(pose, (float) v.x, (float) v.y, (float) v.z)
                    .setColor(255, 255, 255, 255)
                    .setUv(uvs[i][0], vOffset + uvs[i][1])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
