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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CultistClericRenderer extends HumanoidMobRenderer<EntityCultistCleric, CultistClericRenderer.State, HumanoidModel<CultistClericRenderer.State>> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/cultist.png");
    private static final float SHADOW_RADIUS = 0.5F;
    private static final float HOVER_HEIGHT = 0.2F;
    private static final float BOB_AMPLITUDE = 0.11F;
    private static final float BOB_PERIOD_TICKS = 70.0F;
    private static final float PHASE_SPREAD = 0.618034F;
    private static final float HEAD_CLEARANCE = 0.2F;
    private static final float DRAW_OUT_TICKS = 10.0F;
    private static final int LINE_COLOR = ARGB.color(255, 30, 10, 42);
    private static final float LINE_SPEED = -0.025F;
    private static final float LINE_WIDTH = 0.14F;

    public CultistClericRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TTModelLayers.CULTIST)), SHADOW_RADIUS);
        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(EntityCultistCleric entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ritualist = entity.isRitualist();
        if (!state.ritualist) {
            state.bob = 0.0F;
            return;
        }
        float phase = Mth.frac(entity.getId() * PHASE_SPREAD) * BOB_PERIOD_TICKS;
        state.bob = HOVER_HEIGHT + BOB_AMPLITUDE * Mth.sin((state.ageInTicks + phase) / BOB_PERIOD_TICKS * Mth.TWO_PI);
        state.lineStartY = entity.getBbHeight() + HEAD_CLEARANCE + state.bob;
        Vec3 focus = entity.ritualFocus();
        state.lineTo = new Vec3(focus.x - state.x, focus.y - state.y - state.lineStartY, focus.z - state.z);
        state.time = FloatyLineRenderer.time(entity.level().getGameTime(), partialTicks);
        state.lineFade = Mth.clamp(state.ageInTicks / DRAW_OUT_TICKS, 0.0F, 1.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, state.bob, 0.0F);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
        if (!state.ritualist) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, state.lineStartY, 0.0F);
        FloatyLineRenderer.submitShaded(poseStack, collector, state.lineTo, state.time, LINE_COLOR, LINE_SPEED, state.lineFade, LINE_WIDTH);
        poseStack.popPose();
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
