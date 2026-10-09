package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class GolemBuilderRenderer implements BlockEntityRenderer<BlockEntityGolemBuilder, GolemBuilderRenderState> {
    public static final Identifier MODEL = TTIds.rl("models/mesh/golembuilder.ttmesh");

    private static final Identifier TEXTURE = TTIds.rl("textures/entity/golembuilder.png");
    private static final SpriteId LAVA_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/lava_still"));
    private static final String PRESS_PART = "press";
    private static final int WHITE = 0xFFFFFFFF;
    private static final float CENTER = 0.5F;
    private static final float YAW_SOUTH = 180.0F;
    private static final float YAW_WEST = 90.0F;
    private static final float YAW_EAST = 270.0F;
    private static final float PRESS_TRAVEL = 0.625F;
    private static final float LAVA_HEIGHT = 0.625F;
    private static final float LAVA_HALF_WIDTH = 0.3125F;
    private static final float LAVA_NEAR = 0.6875F;
    private static final float LAVA_FAR = 1.3125F;
    private static final int LAVA_LIGHT = 200;

    private final SpriteGetter sprites;

    public GolemBuilderRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public GolemBuilderRenderState createRenderState() {
        return new GolemBuilderRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityGolemBuilder builder, GolemBuilderRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(builder, state, partialTicks, cameraPosition, breakProgress);
        state.facing = builder.getBlockState().getValue(BlockGolemBuilder.FACING);
        state.press = builder.press;
    }

    @Override
    public void submit(GolemBuilderRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw(state.facing)));
        submitParts(state.press, poseStack, collector, state.lightCoords);
        TextureAtlasSprite lava = sprites.get(LAVA_SPRITE);
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> lavaQuad(pose, lava.wrap(buffer)));
        poseStack.popPose();
    }

    public static void submitParts(int press, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TTMesh mesh = GolemMeshes.get(MODEL);
        RenderType type = RenderTypes.entityCutout(TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> drawParts(mesh, pose, buffer, light, false));
        poseStack.pushPose();
        poseStack.translate(0.0F, -PRESS_TRAVEL * Mth.sin(press * Mth.DEG_TO_RAD), 0.0F);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> drawParts(mesh, pose, buffer, light, true));
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityGolemBuilder builder) {
        return builder.renderBounds();
    }

    private static void drawParts(TTMesh mesh, PoseStack.Pose pose, VertexConsumer buffer, int light, boolean pressParts) {
        for (TTMeshPart part : mesh.parts()) {
            if (PRESS_PART.equals(part.name()) == pressParts) {
                GolemMeshes.renderPart(part, pose, buffer, light, WHITE);
            }
        }
    }

    private static float yaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> YAW_SOUTH;
            case WEST -> YAW_WEST;
            case EAST -> YAW_EAST;
            default -> 0.0F;
        };
    }

    private static void lavaQuad(PoseStack.Pose pose, VertexConsumer buffer) {
        lavaVertex(pose, buffer, -LAVA_HALF_WIDTH, LAVA_FAR, 1.0F, 1.0F);
        lavaVertex(pose, buffer, LAVA_HALF_WIDTH, LAVA_FAR, 0.0F, 1.0F);
        lavaVertex(pose, buffer, LAVA_HALF_WIDTH, LAVA_NEAR, 0.0F, 0.0F);
        lavaVertex(pose, buffer, -LAVA_HALF_WIDTH, LAVA_NEAR, 1.0F, 0.0F);
    }

    private static void lavaVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float z, float u, float v) {
        buffer.addVertex(pose, x, LAVA_HEIGHT, z).setColor(WHITE).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LAVA_LIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
