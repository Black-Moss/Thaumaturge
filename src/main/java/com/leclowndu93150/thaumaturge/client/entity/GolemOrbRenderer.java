package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

public final class GolemOrbRenderer extends EntityRenderer<EntityGolemOrb, GolemOrbRenderer.State> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float PULSE_RATE_DIVISOR = 5.0F;
    private static final float PULSE_AMPLITUDE = 0.2F;
    private static final float PULSE_OFFSET = 0.2F;
    private static final float HALF = 0.5F;
    private static final int TINT = ARGB.white(0.8F);

    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityGolemOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tick = entity.tickCount;
        state.red = entity.isRed();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        Identifier texture = state.red ? ParticleTextures.GOLEM_ORB_RED : ParticleTextures.GOLEM_ORB_BLUE;
        int frame = Math.floorMod(state.tick, ParticleTextures.GOLEM_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float pulse = 1.0F + (Mth.sin(state.tick / PULSE_RATE_DIVISOR) * PULSE_AMPLITUDE + PULSE_OFFSET);
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        poseStack.scale(pulse, pulse, pulse);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(texture), (pose, buffer) -> writeQuad(buffer, pose.pose(), u0, u1));
        poseStack.popPose();
    }

    private static void writeQuad(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1) {
        buffer.addVertex(matrix, -HALF, -HALF, 0.0F).setUv(u0, StripUv.V1).setColor(TINT).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, HALF, -HALF, 0.0F).setUv(u1, StripUv.V1).setColor(TINT).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, HALF, HALF, 0.0F).setUv(u1, StripUv.V0).setColor(TINT).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -HALF, HALF, 0.0F).setUv(u0, StripUv.V0).setColor(TINT).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    public static final class State extends EntityRenderState {
        public int tick;
        public boolean red;
    }
}
