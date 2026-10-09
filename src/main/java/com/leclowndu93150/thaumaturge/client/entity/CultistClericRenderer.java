package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CultistClericRenderer extends HumanoidMobRenderer<EntityCultistCleric, CultistClericRenderer.State, HumanoidModel<CultistClericRenderer.State>> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/cultist.png");
    private static final float SHADOW_RADIUS = 0.5F;
    private static final int PHASE_MODULUS = 1000;
    private static final float BOB_BASE = 0.21F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float BOB_RATE_DIVISOR = 9.0F;
    private static final float LINE_START_EYE_FACTOR = 1.2F;
    private static final double ANCHOR_CENTER = 0.5;
    private static final double ANCHOR_LIFT = 1.5;
    private static final float FADE_TICKS = 10.0F;
    private static final int LINE_COLOR = 0x110011;
    private static final float LINE_SPEED = -0.03F;
    private static final float LINE_WIDTH = 0.25F;

    public CultistClericRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TTModelLayers.CULTIST)), SHADOW_RADIUS);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCultistCleric entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ritualist = entity.isRitualist();
        if (!state.ritualist) {
            return;
        }
        float cycle = state.ageInTicks + entity.getId() % PHASE_MODULUS;
        state.bob = BOB_BASE + BOB_AMPLITUDE * Mth.sin(cycle / BOB_RATE_DIVISOR);
        state.lineStartY = state.eyeHeight * LINE_START_EYE_FACTOR;
        BlockPos anchor = entity.ritualAnchor();
        state.lineTo = new Vec3(anchor.getX() + ANCHOR_CENTER - state.x, anchor.getY() + ANCHOR_LIFT - (state.y + state.bob + state.lineStartY), anchor.getZ() + ANCHOR_CENTER - state.z);
        state.time = FloatyLineRenderer.time(entity.level().getGameTime(), partialTicks);
        state.lineFade = Math.min(entity.tickCount, FADE_TICKS) / FADE_TICKS;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.ritualist) {
            super.submit(state, poseStack, collector, camera);
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, state.bob, 0.0F);
        super.submit(state, poseStack, collector, camera);
        poseStack.translate(0.0F, state.lineStartY, 0.0F);
        FloatyLineRenderer.submit(poseStack, collector, state.lineTo, state.time, LINE_COLOR, LINE_SPEED, state.lineFade, LINE_WIDTH);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    public static final class State extends HumanoidRenderState {
        public boolean ritualist;
        public float bob;
        public float lineStartY;
        public Vec3 lineTo = Vec3.ZERO;
        public float time;
        public float lineFade;
    }
}
