package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortals;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    private static final float HEIGHT = 2.9F;
    private static final float WIDTH = 2.3F;
    private static final float OPENING_TICKS = 50.0F;
    private static final float SLIT_WIDTH = 0.04F;
    private static final float FRAMES_PER_TICK = 1.0F;
    private static final float HALF = 0.5F;

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
        capture(state, entity, entity.isActive(), entity.activeCounter + partialTicks, entity.pulse, partialTicks);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        submitPortal(state, poseStack, collector, camera, HEIGHT, WIDTH);
    }

    static void capture(State state, LivingEntity portal, boolean open, float openTicks, int pulseTicks, float partialTicks) {
        state.open = open;
        state.opening = Mth.clamp(openTicks / OPENING_TICKS, 0.0F, 1.0F);
        state.flinch = portal.hurtDuration > 0 ? Mth.clamp((portal.hurtTime - partialTicks) / portal.hurtDuration, 0.0F, 1.0F) : 0.0F;
        float pulseLeft = pulseTicks - partialTicks;
        state.beat = pulseLeft > 0.0F ? 1.0F - pulseLeft / CultistPortals.PULSE_TICKS : 0.0F;
        state.damage = Mth.clamp(1.0F - portal.getHealth() / portal.getMaxHealth(), 0.0F, 1.0F);
        state.centre = portal.getBbHeight() * HALF;
    }

    static void submitPortal(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float height, float width) {
        if (!state.open) {
            return;
        }
        float time = state.ageInTicks;
        float eased = state.opening * state.opening * (3.0F - 2.0F * state.opening);
        float halfHeight = height * HALF * PortalWave.heightScale(state.damage, time, state.flinch, state.beat);
        float halfWidth = width * HALF * Mth.lerp(eased, SLIT_WIDTH, 1.0F) * PortalWave.widthScale(state.damage, time, state.flinch, state.beat);
        int tint = ARGB.white(PortalWave.opacity(state.damage, time));
        int frame = Math.floorMod((int) (time * FRAMES_PER_TICK), ParticleTextures.CULTIST_PORTAL_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.CULTIST_PORTAL_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.CULTIST_PORTAL_FRAMES);
        poseStack.pushPose();
        poseStack.translate(0.0F, state.centre, 0.0F);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.translucent(ParticleTextures.CULTIST_PORTAL), (pose, buffer) -> writeQuad(buffer, pose.pose(), halfWidth, halfHeight, u0, u1, tint));
        poseStack.popPose();
    }

    private static void writeQuad(VertexConsumer buffer, Matrix4fc matrix, float halfWidth, float halfHeight, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -halfWidth, -halfHeight, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, halfWidth, -halfHeight, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, halfWidth, halfHeight, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -halfWidth, halfHeight, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    public static final class State extends EntityRenderState {
        public boolean open;
        public float opening;
        public float flinch;
        public float beat;
        public float damage;
        public float centre;
    }
}
