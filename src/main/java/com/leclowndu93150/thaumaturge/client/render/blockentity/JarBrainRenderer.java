package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarBrain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
    private static final float BRAIN_ANCHOR_Y = 0.81F;
    private static final float BRINE_ANCHOR_Y = 0.01F;
    private static final float BRAIN_SCALE = 0.4F;
    private static final float BOB_PERIOD = 14.0F;
    private static final float BOB_AMPLITUDE = 0.03F;
    private static final float XP_PERIOD = 5.0F;
    private static final float QUARTER_TURN = 90.0F;
    private static final float HALF_TURN = 180.0F;

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
        float delta = jar.rota - jar.rotb;
        delta -= Mth.TWO_PI * Mth.floor((delta + Mth.PI) / Mth.TWO_PI);
        state.yawRadians = jar.rotb + delta * partialTicks;
        LocalPlayer player = Minecraft.getInstance().player;
        float time = (player == null ? 0 : player.tickCount) + partialTicks;
        state.bobOffset = BOB_AMPLITUDE * Mth.sin(time / BOB_PERIOD) + BOB_AMPLITUDE;
        state.ageInTicks = time;
        state.xpResponse = Mth.sin(time / XP_PERIOD) * jar.xp() / BlockEntityJarBrain.XP_MAX;
    }

    @Override
    public void submit(JarBrainRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(CENTER, BRAIN_ANCHOR_Y - state.bobOffset, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(QUARTER_TURN - state.yawRadians * Mth.RAD_TO_DEG));
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        collector.submitModel(brain, state, poseStack, BRAIN_TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(CENTER, BRINE_ANCHOR_Y, CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        collector.submitModelPart(brine.root, poseStack, RenderTypes.entityTranslucent(BRINE_TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }
}
