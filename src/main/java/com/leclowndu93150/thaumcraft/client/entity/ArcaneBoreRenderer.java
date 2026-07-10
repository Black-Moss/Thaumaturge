package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.fx.render.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumcraft.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumcraft.content.entity.construct.EntityArcaneBore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

public final class ArcaneBoreRenderer
        extends MobRenderer<EntityArcaneBore, ArcaneBoreRenderState, ArcaneBoreModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/arcanebore.png");
    private static final Identifier BEAM_TEXTURE = TCIds.rl("textures/misc/beam1.png");
    private static final RenderType BEAM_TYPE = RenderType.create(
            "tc_bore_beam",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE)
                    .withTexture("Sampler0", BEAM_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());

    private static final float SHADOW = 0.5F;
    private static final double BEAM_LENGTH = 5.0;
    private static final float BEAM_RADIUS = 0.15F;
    private static final float BEAM_ALPHA = 0.4F;
    private static final int BEAM_TINT = ARGB.colorFromFloat(BEAM_ALPHA, 0.0F, 1.0F, 0.4F);
    private static final int BEAM_LIGHT = 0x000000C8;
    private static final int BEAM_STRIPS = 3;

    public ArcaneBoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneBoreModel(context.bakeLayer(TCModelLayers.ARCANE_BORE)), SHADOW);
    }

    @Override
    public ArcaneBoreRenderState createRenderState() {
        return new ArcaneBoreRenderState();
    }

    @Override
    public void extractRenderState(EntityArcaneBore entity, ArcaneBoreRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.wrapDegrees(Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot));
        state.bodyRot = 0.0F;
        state.digging = entity.clientDigging && entity.isActive() && entity.validInventory();
        state.headYaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.eyeHeight = entity.getEyeHeight();
        state.animTicks = entity.tickCount + partialTicks;
        state.beamScroll = (float) (entity.level().getGameTime() % 72L * 5L) + 5.0F * partialTicks;
    }

    @Override
    public void submit(ArcaneBoreRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.digging) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, state.eyeHeight, 0.0F);
        float scroll = -state.animTicks;
        float uvOffset = -scroll * 0.2F - Mth.floor(-scroll * 0.1F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F + state.headYaw));
        poseStack.mulPose(Axis.XN.rotationDegrees(state.headPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.beamScroll));
        for (int strip = 0; strip < BEAM_STRIPS; strip++) {
            poseStack.mulPose(Axis.YP.rotationDegrees(60.0F));
            float v0 = -1.0F + uvOffset + strip / 3.0F;
            float v1 = (float) BEAM_LENGTH + v0;
            collector.submitCustomGeometry(poseStack, BEAM_TYPE, (pose, buffer) -> {
                Matrix4fc mat = pose.pose();
                buffer.addVertex(mat, 0.0F, (float) BEAM_LENGTH, 0.0F).setUv(1.0F, v1)
                        .setColor(BEAM_TINT).setLight(BEAM_LIGHT);
                buffer.addVertex(mat, -BEAM_RADIUS, 0.0F, 0.0F).setUv(1.0F, v0)
                        .setColor(BEAM_TINT).setLight(BEAM_LIGHT);
                buffer.addVertex(mat, BEAM_RADIUS, 0.0F, 0.0F).setUv(0.0F, v0)
                        .setColor(BEAM_TINT).setLight(BEAM_LIGHT);
                buffer.addVertex(mat, 0.0F, (float) BEAM_LENGTH, 0.0F).setUv(0.0F, v1)
                        .setColor(BEAM_TINT).setLight(BEAM_LIGHT);
            });
        }
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(ArcaneBoreRenderState state) {
        return TEXTURE;
    }
}
