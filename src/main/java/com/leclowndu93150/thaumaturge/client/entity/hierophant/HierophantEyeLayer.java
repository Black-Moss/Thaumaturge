package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class HierophantEyeLayer extends RenderLayer<HierophantRenderState, HierophantModel> {
    private final HierophantModel eye;

    public HierophantEyeLayer(RenderLayerParent<HierophantRenderState, HierophantModel> parent, ModelPart root) {
        super(parent);
        eye = new HierophantModel(root);
        eye.onlyEye();
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, HierophantRenderState state, float yaw, float pitch) {
        if (state.isInvisible) {
            return;
        }
        collector.order(1).submitModel(eye, state, pose, HierophantRenderTypes.EYE, 0xF000F0, OverlayTexture.NO_OVERLAY, state.awakened ? 0xFFFF6960 : -1, null, state.outlineColor, null);
    }
}
