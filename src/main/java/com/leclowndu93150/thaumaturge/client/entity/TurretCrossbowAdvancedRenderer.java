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
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public final class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced, TurretCrossbowAdvancedRenderer.State> {
    private static final Identifier MESH = TTIds.rl("models/mesh/crossbow_advanced.ttmesh");
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/crossbow_advanced.png");
    private static final String PART_LEGS = "legs";
    private static final String PART_MECH = "mech";
    private static final String PART_BOX = "box";
    private static final String PART_SHIELD = "shield";
    private static final String PART_BRAIN = "brain";
    private static final String PART_LOADER = "loader";
    private static final String PART_BOW_FIRST = "bow1";
    private static final String PART_BOW_SECOND = "bow2";
    private static final float SHADOW_RADIUS = 0.5F;
    private static final float MODEL_LIFT = 0.75F;
    private static final float MINECART_SCALE_XZ = 0.66F;
    private static final float MINECART_SCALE_Y = 0.75F;
    private static final float HURT_GREEN = 0.5F;
    private static final float HURT_BLUE = 0.5F;
    private static final float HURT_JIGGLE_DIVISOR = 500.0F;
    private static final float LOADER_TRAVEL_DIVISOR = 12.0F;
    private static final float BOW_PIVOT_FORWARD = 0.375F;
    private static final float BOW_MAX_ANGLE = 20.0F;
    private static final int WHITE = ARGB.white(1.0F);
    private static final int HURT_TINT = ARGB.colorFromFloat(1.0F, 1.0F, HURT_GREEN, HURT_BLUE);

    private final RandomSource jiggle = RandomSource.create();

    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTurretCrossbowAdvanced entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swingAnim = entity.swingAnim;
        state.loadProgress = entity.getLoadProgress(partialTicks);
        state.ridingMinecart = entity.getVehicle() instanceof AbstractMinecart;
        state.hurtTime = entity.hurtTime;
        state.headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        state.headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.translate(0.0F, MODEL_LIFT, 0.0F);
        submitLegs(state, poseStack, collector);
        submitHead(state, poseStack, collector);
        poseStack.popPose();
    }

    private void submitLegs(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        if (state.ridingMinecart) {
            poseStack.scale(MINECART_SCALE_XZ, MINECART_SCALE_Y, MINECART_SCALE_XZ);
        }
        submitPart(PART_LEGS, poseStack, collector, state.lightCoords, WHITE);
        poseStack.popPose();
    }

    private void submitHead(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        int light = state.lightCoords;
        boolean hurt = state.hurtTime > 0;
        int color = hurt ? HURT_TINT : WHITE;
        poseStack.pushPose();
        if (hurt) {
            float amplitude = state.hurtTime / HURT_JIGGLE_DIVISOR;
            poseStack.translate((float) jiggle.nextGaussian() * amplitude, (float) jiggle.nextGaussian() * amplitude, (float) jiggle.nextGaussian() * amplitude);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.headYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.headPitch));
        submitPart(PART_MECH, poseStack, collector, light, color);
        submitPart(PART_BOX, poseStack, collector, light, color);
        submitPart(PART_SHIELD, poseStack, collector, light, color);
        submitPart(PART_BRAIN, poseStack, collector, light, color);
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, Mth.sin(Mth.TWO_PI * Mth.sqrt(state.loadProgress)) / LOADER_TRAVEL_DIVISOR);
        submitPart(PART_LOADER, poseStack, collector, light, color);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, BOW_PIVOT_FORWARD);
        float angle = Mth.sin(Mth.TWO_PI * Mth.sqrt(state.swingAnim)) * BOW_MAX_ANGLE;
        submitBow(PART_BOW_FIRST, angle, poseStack, collector, light, color);
        submitBow(PART_BOW_SECOND, -angle, poseStack, collector, light, color);
        poseStack.popPose();
        poseStack.popPose();
    }

    private void submitBow(String name, float angle, PoseStack poseStack, SubmitNodeCollector collector, int light, int color) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        submitPart(name, poseStack, collector, light, color);
        poseStack.popPose();
    }

    private void submitPart(String name, PoseStack poseStack, SubmitNodeCollector collector, int light, int color) {
        for (TTMeshPart part : GolemMeshes.get(MESH).parts()) {
            if (part.name().equals(name)) {
                collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, color));
                return;
            }
        }
    }

    public static final class State extends TurretCrossbowRenderState {
        public float headYaw;
        public float headPitch;

        public State() {}
    }
}
