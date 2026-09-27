package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class TaintOverlayLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    public TaintOverlayLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        if (!((TaintOverlayRenderState) state).thaumaturge$taintOverlay()) {
            return;
        }
        M model = this.getParentModel();
        collector.order(1).submitModel(model, state, poseStack, RenderTypes.entityTranslucent(TaintOverlayTextures.get(model)), lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1,
                null, state.outlineColor, null);
    }
}
