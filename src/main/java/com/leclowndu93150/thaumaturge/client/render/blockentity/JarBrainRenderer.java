package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarBrain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class JarBrainRenderer implements BlockEntityRenderer<BlockEntityJarBrain, JarBrainRenderState> {
    private static final Identifier BRAIN_TEXTURE = TTIds.rl("textures/entity/brain2.png");
    private static final Identifier BRINE_TEXTURE = TTIds.rl("textures/entity/jarbrine.png");
    private static final float CENTER = 0.5F;
    private static final float BRINE_ANCHOR_Y = 0.01F;
    private static final float XP_PERIOD = 5.0F;
    private static final float QUARTER_TURN = 90.0F;
    private static final float HALF_TURN = 180.0F;
    private static final float BRAIN_ANCHOR_Y = 0.405F;
    private static final float BRAIN_SCALE = 0.4F;
    private static final float BRAIN_PIVOT_Y = 0.9375F;
    private static final float BOB_PERIOD = 14.0F;
    private static final float BOB_AMPLITUDE = 0.03F;
    private static final int OUTLINE_NONE = 0;

    private final BrainModel brain;
    private final JarBrineModel brine;

    public JarBrainRenderer(BlockEntityRendererProvider.Context context) {
        this.brain = new BrainModel(context.bakeLayer(TTModelLayers.BRAIN));
        this.brine = new JarBrineModel(context.bakeLayer(TTModelLayers.JAR_BRINE));
    }

    @Override
    public JarBrainRenderState createRenderState() {
        return new JarBrainRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityJarBrain jar, JarBrainRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(jar, state, partialTicks, cameraPosition, breakProgress);
        LocalPlayer player = Minecraft.getInstance().player;
        state.ageInTicks = (player == null ? 0 : player.tickCount) + partialTicks;
        state.yawRadians = Mth.rotLerpRad(partialTicks, jar.rotb, jar.rota);
        state.bobOffset = -BOB_AMPLITUDE * Mth.sin(state.ageInTicks / BOB_PERIOD);
        float fill = (float) jar.xp() / BlockEntityJarBrain.XP_MAX;
        state.xpResponse = fill * Mth.sin(state.ageInTicks / XP_PERIOD);
    }

    @Override
    public void submit(JarBrainRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseBrain(poseStack, state.bobOffset, -(state.yawRadians * Mth.RAD_TO_DEG + QUARTER_TURN));
        collector.submitModel(brain, state, poseStack, BRAIN_TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, OUTLINE_NONE, null);
        poseStack.popPose();
        poseStack.pushPose();
        poseBrine(poseStack);
        collector.submitModelPart(brine.root, poseStack, TTFlatRenderTypes.entityTranslucentNoDepthWrite(BRINE_TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    public static void poseBrine(PoseStack poseStack) {
        poseStack.translate(CENTER, BRINE_ANCHOR_Y, CENTER);
        poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_TURN));
    }

    public static void poseBrain(PoseStack poseStack, float lift, float turnDegrees) {
        poseStack.translate(CENTER, BRAIN_ANCHOR_Y + lift, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(turnDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_TURN));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        poseStack.translate(0.0F, -BRAIN_PIVOT_Y, 0.0F);
    }

}
