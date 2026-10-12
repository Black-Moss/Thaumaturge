package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4fc;

public final class CultistPortalRenderer extends EntityRenderer<EntityCultistPortalLesser, CultistPortalRenderer.State> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float HALF_HEIGHT = 1.4F;
    private static final float WIDTH_FACTOR = 1.25F;
    private static final float FULL_GROWTH = 50.0F;
    private static final float HALF = 0.5F;
    private static final float BOB_HEIGHT_DIVISOR = 4.0F;
    private static final float BOB_WIDTH_DIVISOR = 3.0F;
    private static final float DAMAGE_DIVISOR = 3.0F;
    private static final float BOB_PERIOD_BASE = 5.0F;
    private static final float BOB_PERIOD_SHRINK = 12.0F;
    private static final float BOB2_PERIOD_BASE = 6.0F;
    private static final float BOB2_PERIOD_SHRINK = 15.0F;
    private static final float HURT_DEGREES_PER_TICK = 72.0F;
    private static final float HURT_HEIGHT_DIVISOR = 4.0F;
    private static final float HURT_GROWTH = 6.0F;
    private static final float PULSE_DEGREES_PER_TICK = 36.0F;
    private static final float PULSE_HEIGHT_DIVISOR = 4.0F;
    private static final float PULSE_GROWTH = 12.0F;
    private static final float HALF_TURN = 180.0F;
    private static final int LAST_FRAME = ParticleTextures.CULTIST_PORTAL_FRAMES - 1;

    public CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCultistPortalLesser entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        capture(state, entity, entity.isActive(), entity.activeCounter + partialTicks, entity.pulse);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        submitPortal(state, poseStack, collector, camera, HALF_HEIGHT, WIDTH_FACTOR);
    }

    static void capture(State state, LivingEntity portal, boolean active, float counter, int pulse) {
        state.active = active;
        state.counter = counter;
        state.hurtTime = portal.hurtTime;
        state.pulse = pulse;
        state.healthFraction = portal.getHealth() / portal.getMaxHealth();
        state.halfBoxHeight = portal.getBbHeight() * HALF;
    }

    static void submitPortal(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float baseHalfHeight, float widthFactor) {
        if (!state.active) {
            return;
        }
        float counter = state.counter;
        float grown = Math.min(FULL_GROWTH, counter);
        float damage = (1.0F - state.healthFraction) / DAMAGE_DIVISOR;
        float bob = Mth.sin(counter / (BOB_PERIOD_BASE - BOB_PERIOD_SHRINK * damage)) * damage + damage;
        float bob2 = Mth.sin(counter / (BOB2_PERIOD_BASE - BOB2_PERIOD_SHRINK * damage)) * damage + damage;
        float halfHeight = baseHalfHeight;
        if (state.hurtTime > 0) {
            float hurt = Mth.sin(state.hurtTime * HURT_DEGREES_PER_TICK * Mth.DEG_TO_RAD);
            halfHeight -= hurt / HURT_HEIGHT_DIVISOR;
            grown += HURT_GROWTH * hurt;
        }
        if (state.pulse > 0) {
            float beat = Mth.sin(state.pulse * PULSE_DEGREES_PER_TICK * Mth.DEG_TO_RAD);
            halfHeight += beat / PULSE_HEIGHT_DIVISOR;
            grown += PULSE_GROWTH * beat;
        }
        halfHeight -= bob / BOB_HEIGHT_DIVISOR;
        float halfWidth = Math.max(0.0F, grown / FULL_GROWTH * widthFactor - bob2 / BOB_WIDTH_DIVISOR);
        float finalHalfHeight = halfHeight;
        int tint = ARGB.white(1.0F - bob);
        int frame = LAST_FRAME - Math.floorMod((int) Math.floor(counter), ParticleTextures.CULTIST_PORTAL_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.CULTIST_PORTAL_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.CULTIST_PORTAL_FRAMES);
        poseStack.pushPose();
        poseStack.translate(0.0F, state.halfBoxHeight, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN - camera.yRot));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.translucent(ParticleTextures.CULTIST_PORTAL), (pose, buffer) -> writeQuad(buffer, pose.pose(), halfWidth, finalHalfHeight, u0, u1, tint));
        poseStack.popPose();
    }

    private static void writeQuad(VertexConsumer buffer, Matrix4fc matrix, float halfWidth, float halfHeight, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -halfWidth, -halfHeight, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, halfWidth, -halfHeight, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, halfWidth, halfHeight, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -halfWidth, halfHeight, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    public static final class State extends EntityRenderState {
        public boolean active;
        public float counter;
        public int hurtTime;
        public int pulse;
        public float healthFraction;
        public float halfBoxHeight;
    }
}
