package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4fc;

public final class AspectOrbRenderer extends EntityRenderer<EntityAspectOrb, AspectOrbRenderer.State> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final int FRAMES_PER_TICK = 2;
    private static final float MIN_SCALE = 0.1F;
    private static final float SCALE_RANGE = 0.3F;
    private static final float ALPHA = 0.5F;
    private static final float CHANNEL_MAX = 255.0F;
    private static final float LEFT = -0.5F;
    private static final float RIGHT = 0.5F;
    private static final float BOTTOM = -0.25F;
    private static final float TOP = 0.75F;

    public AspectOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityAspectOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tick = entity.tickCount;
        state.age = entity.getAge();
        state.color = entity.getAspectColor();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float scale = MIN_SCALE + SCALE_RANGE * ((EntityAspectOrb.MAX_AGE - state.age) / (float) EntityAspectOrb.MAX_AGE);
        int frame = Math.floorMod(state.tick * FRAMES_PER_TICK, ParticleTextures.ORB_GLOW_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.ORB_GLOW_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.ORB_GLOW_FRAMES);
        int tint = ARGB.colorFromFloat(ALPHA, ARGB.red(state.color) / CHANNEL_MAX, ARGB.green(state.color) / CHANNEL_MAX, ARGB.blue(state.color) / CHANNEL_MAX);
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        poseStack.scale(scale, scale, scale);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.ORB_GLOW), (pose, buffer) -> writeQuad(buffer, pose.pose(), u0, u1, tint));
        poseStack.popPose();
    }

    private static void writeQuad(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1, int tint) {
        buffer.addVertex(matrix, LEFT, BOTTOM, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, RIGHT, BOTTOM, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, RIGHT, TOP, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, LEFT, TOP, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    public static final class State extends EntityRenderState {
        public int tick;
        public int age;
        public int color;
    }
}
