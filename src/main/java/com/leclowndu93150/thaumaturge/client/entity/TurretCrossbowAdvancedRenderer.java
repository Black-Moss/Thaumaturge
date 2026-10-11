package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public final class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced, TurretCrossbowAdvancedRenderer.State> {
    private static final Identifier MESH = TTIds.rl("models/mesh/crossbow_advanced.ttmesh");
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/crossbow_advanced.png");
    private static final String LEGS = "legs";
    private static final String LOADER = "loader";
    private static final String BOW_RIGHT = "bow1";
    private static final String BOW_LEFT = "bow2";

    private static final float SHADOW = 0.5F;
    private static final float MESH_SCALE = 0.9F;
    private static final float STAND_HEIGHT = 0.765F;
    private static final float CART_LEG_REACH = 0.65F;
    private static final float CART_LEG_LENGTH = 0.45F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int HURT_TINT = 0xFFFFB0AA;
    private static final float JIGGLE = 0.035F;
    private static final float LOADER_TRAVEL = 0.14F;
    private static final float BOW_PIVOT_Y = 0.094F;
    private static final float BOW_PIVOT_Z = 0.06F;
    private static final float BOW_SWING_DEGREES = 28.0F;

    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTurretCrossbowAdvanced entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.loadProgress = entity.getLoadProgress(partialTicks);
        state.swingAnim = entity.swingAnim;
        state.hurtTime = entity.hurtTime;
        state.ridingMinecart = entity.getVehicle() instanceof AbstractMinecart;
        state.hurtFade = entity.hurtDuration > 0 ? Mth.clamp((entity.hurtTime - partialTicks) / entity.hurtDuration, 0.0F, 1.0F) : 0.0F;
        if (state.hurtFade > 0.0F) {
            RandomSource random = entity.getRandom();
            float strength = JIGGLE * state.hurtFade;
            state.jiggleX = (random.nextFloat() * 2.0F - 1.0F) * strength;
            state.jiggleY = (random.nextFloat() * 2.0F - 1.0F) * strength;
            state.jiggleZ = (random.nextFloat() * 2.0F - 1.0F) * strength;
        } else {
            state.jiggleX = 0.0F;
            state.jiggleY = 0.0F;
            state.jiggleZ = 0.0F;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float legLength = state.ridingMinecart ? CART_LEG_LENGTH : 1.0F;
        float legReach = state.ridingMinecart ? CART_LEG_REACH : 1.0F;
        int light = state.lightCoords;
        int upperTint = state.hurtFade > 0.0F ? HURT_TINT : WHITE;
        RenderType type = RenderTypes.entityCutout(TEXTURE);
        float bowSwing = BOW_SWING_DEGREES * snap(state.swingAnim);
        float loaderShift = LOADER_TRAVEL * snap(state.loadProgress);
        poseStack.pushPose();
        poseStack.translate(0.0F, STAND_HEIGHT * MESH_SCALE * legLength, 0.0F);
        poseStack.scale(MESH_SCALE, MESH_SCALE, MESH_SCALE);
        for (TTMeshPart part : GolemMeshes.get(MESH).parts()) {
            poseStack.pushPose();
            if (LEGS.equals(part.name())) {
                poseStack.scale(legReach, legLength, legReach);
                submitPart(collector, poseStack, type, part, light, WHITE);
            } else {
                poseStack.translate(state.jiggleX, state.jiggleY, state.jiggleZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
                placeMovingPart(poseStack, part.name(), bowSwing, loaderShift);
                submitPart(collector, poseStack, type, part, light, upperTint);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void placeMovingPart(PoseStack poseStack, String name, float bowSwing, float loaderShift) {
        if (LOADER.equals(name)) {
            poseStack.translate(0.0F, 0.0F, loaderShift);
        } else if (BOW_RIGHT.equals(name)) {
            swingBow(poseStack, bowSwing);
        } else if (BOW_LEFT.equals(name)) {
            swingBow(poseStack, -bowSwing);
        }
    }

    private static void swingBow(PoseStack poseStack, float degrees) {
        poseStack.translate(0.0F, BOW_PIVOT_Y, BOW_PIVOT_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        poseStack.translate(0.0F, -BOW_PIVOT_Y, -BOW_PIVOT_Z);
    }

    private static void submitPart(SubmitNodeCollector collector, PoseStack poseStack, RenderType type, TTMeshPart part, int light, int color) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, color));
    }

    private static float snap(float progress) {
        return progress <= 0.0F ? 0.0F : Mth.sin(Mth.PI * Mth.sqrt(Math.min(progress, 1.0F)));
    }

    public static final class State extends TurretCrossbowRenderState {
        public float hurtFade;
        public float jiggleX;
        public float jiggleY;
        public float jiggleZ;
    }
}
