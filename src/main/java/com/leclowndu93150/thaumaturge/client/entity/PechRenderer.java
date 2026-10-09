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
    private static final float RIGHT_HAND_X = 0.0625F;
    private static final float LEFT_HAND_X = -0.0625F;
    private static final float HAND_Y = 0.025F;
    private static final float HAND_Z = -0.5625F;
    private static final float BOW_OFFSET_X = -0.075F;
    private static final float BOW_OFFSET_Y = -0.1F;
    private static final float ITEM_PITCH_DEGREES = -90.0F;
    private static final float ITEM_YAW_DEGREES = 180.0F;

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

    private static void submitHand(PechRenderState state, ItemStackRenderState item, HumanoidArm hand, ModelPart arm, PoseStack poseStack, SubmitNodeCollector collector) {
        if (item.isEmpty()) {
            return;
        }
        float x = hand == HumanoidArm.RIGHT ? RIGHT_HAND_X : LEFT_HAND_X;
        float y = HAND_Y;
        if (state.holdingBow && state.mainArm == hand) {
            x += BOW_OFFSET_X;
            y += BOW_OFFSET_Y;
        }
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        poseStack.translate(x, y, HAND_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(ITEM_PITCH_DEGREES));
        poseStack.mulPose(Axis.YP.rotationDegrees(ITEM_YAW_DEGREES));
        item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(PechRenderState state) {
        return TEXTURES[Math.clamp(state.pechType, 0, TEXTURES.length - 1)];
    }
}
