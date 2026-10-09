package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;

public final class MindSpiderRenderer extends MobRenderer<EntityMindSpider, MindSpiderRenderState, SpiderModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final Identifier EYES_TEXTURE = Identifier.withDefaultNamespace("textures/entity/spider/spider_eyes.png");
    private static final RenderType EYES = RenderTypes.eyes(EYES_TEXTURE);
    private static final float SHADOW_RADIUS = 0.5F;
    private static final float BODY_SCALE = 0.6F;
    private static final float MAX_ALPHA = 0.1F;
    private static final float FADE_IN_DIVISOR = 100.0F;
    private static final int EYES_ORDER = 1;

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), SHADOW_RADIUS);
        this.addLayer(new EyesLayer(this));
    }

    @Override
    public MindSpiderRenderState createRenderState() {
        return new MindSpiderRenderState();
    }

    @Override
    public void extractRenderState(EntityMindSpider entity, MindSpiderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        LocalPlayer player = Minecraft.getInstance().player;
        String localName = player == null ? "" : player.getGameProfile().name();
        String viewer = entity.witnessName();
        state.hiddenFromViewer = !viewer.isEmpty() && !viewer.equals(localName);
        this.shadowRadius = state.hiddenFromViewer ? 0.0F : SHADOW_RADIUS;
    }

    @Override
    public void submit(MindSpiderRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.hiddenFromViewer) {
            return;
        }
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void scale(MindSpiderRenderState state, PoseStack poseStack) {
        poseStack.scale(BODY_SCALE, BODY_SCALE, BODY_SCALE);
    }

    @Override
    protected RenderType getRenderType(MindSpiderRenderState state, boolean visible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getModelTint(MindSpiderRenderState state) {
        return ARGB.white(Math.min(MAX_ALPHA, state.ageInTicks / FADE_IN_DIVISOR));
    }

    @Override
    public Identifier getTextureLocation(MindSpiderRenderState state) {
        return TEXTURE;
    }

    private static final class EyesLayer extends RenderLayer<MindSpiderRenderState, SpiderModel> {
        private EyesLayer(RenderLayerParent<MindSpiderRenderState, SpiderModel> renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, MindSpiderRenderState state, float yRot, float xRot) {
            collector.order(EYES_ORDER).submitModel(getParentModel(), state, poseStack, EYES, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
