package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.PechModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;

public final class PechRenderer extends MobRenderer<EntityPech, PechRenderState, PechModel> {
    private static final Identifier[] TEXTURES = {TTIds.rl("textures/entity/pech_forage.png"), TTIds.rl("textures/entity/pech_thaum.png"), TTIds.rl("textures/entity/pech_stalker.png")};
    private static final float SHADOW_RADIUS = 0.5F;
    private static final float MODEL_FLIP = -1.0F;
    private static final float MODEL_LIFT = -1.501F;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float FIST_CENTRE_X = 1.0F * PIXEL;
    private static final float FIST_DROP = 5.0F * PIXEL;
    private static final float FIST_FORWARD = 1.0F * PIXEL;
    private static final float GRIP_PITCH_DEGREES = -90.0F;
    private static final float GRIP_TURN_DEGREES = 180.0F;
    private static final float HELD_ITEM_SCALE = 0.8F;
    private static final float BOW_SHIFT_OUT = 0.5F * PIXEL;
    private static final float BOW_SHIFT_UP = 1.5F * PIXEL;

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(TTModelLayers.PECH)), SHADOW_RADIUS);
    }

    @Override
    public PechRenderState createRenderState() {
        return new PechRenderState();
    }

    @Override
    public void extractRenderState(EntityPech entity, PechRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
        state.pechType = entity.variant();
        state.mumble = entity.chatterLevel;
        state.holdingBow = entity.getMainHandItem().is(Items.BOW);
    }

    @Override
    public void submit(PechRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.rightHandItemState.isEmpty() && state.leftHandItemState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(state.scale, state.scale, state.scale);
        setupRotations(state, poseStack, state.bodyRot, state.scale);
        poseStack.scale(MODEL_FLIP, MODEL_FLIP, 1.0F);
        poseStack.translate(0.0F, MODEL_LIFT, 0.0F);
        model.setupAnim(state);
        submitHand(state, state.rightHandItemState, HumanoidArm.RIGHT, model.rightArm, poseStack, collector);
        submitHand(state, state.leftHandItemState, HumanoidArm.LEFT, model.leftArm, poseStack, collector);
        poseStack.popPose();
    }

    private static void submitHand(PechRenderState state, ItemStackRenderState item, HumanoidArm arm, ModelPart armPart, PoseStack poseStack, SubmitNodeCollector collector) {
        if (item.isEmpty()) {
            return;
        }
        float outward = arm == HumanoidArm.LEFT ? 1.0F : -1.0F;
        poseStack.pushPose();
        armPart.translateAndRotate(poseStack);
        poseStack.translate(outward * FIST_CENTRE_X, FIST_DROP, -FIST_FORWARD);
        if (state.holdingBow && arm == state.mainArm) {
            poseStack.translate(outward * BOW_SHIFT_OUT, -BOW_SHIFT_UP, 0.0F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(GRIP_PITCH_DEGREES));
        poseStack.mulPose(Axis.YP.rotationDegrees(GRIP_TURN_DEGREES));
        poseStack.scale(HELD_ITEM_SCALE, HELD_ITEM_SCALE, HELD_ITEM_SCALE);
        item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(PechRenderState state) {
        return TEXTURES[Math.clamp(state.pechType, 0, TEXTURES.length - 1)];
    }
}
