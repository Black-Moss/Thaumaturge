package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class TaintOverlayLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final int SKIN_ORDER = 1;
    private static final int GLOW_ORDER = 2;
    private static final int NO_TINT = -1;

    private final LivingEntityRenderer<?, S, M> renderer;

    public TaintOverlayLayer(LivingEntityRenderer<?, S, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        if (state.isInvisible || !((TaintOverlayRenderState) state).thaumaturge$taintOverlay()) {
            return;
        }
        M model = this.getParentModel();
        TaintSkin skin = TaintSkins.get(model, renderer.getTextureLocation(state));
        if (skin == null) {
            return;
        }
        RenderType skinType = skin.replacesBase() ? model.renderType(skin.texture()) : RenderTypes.entityTranslucent(skin.texture());
        collector.order(SKIN_ORDER).submitModel(model, state, poseStack, skinType, lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), NO_TINT, null, state.outlineColor, null);
        if (skin.glow() != null) {
            collector.order(GLOW_ORDER).submitModel(model, state, poseStack, RenderTypes.eyes(skin.glow()), lightCoords, OverlayTexture.NO_OVERLAY, NO_TINT, null, state.outlineColor, null);
        }
    }
}
