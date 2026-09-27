package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;

public final class TaintSporeSwarmerCoreLayer extends RenderLayer<TaintSporeRenderState, TaintSporeSwarmerModel> {
    private static final float SIZE_SCALE = 0.07F;
    private static final float PULSE_SCALE = 0.025F;
    private static final float PULSE_RATE = 0.075F;
    private static final float MIN_SCALE = 0.01F;
    private static final float LEGACY_LIFT = 1.6F;
    private static final float CALM_WOBBLE = 0.02F;
    private static final float HURT_WOBBLE = 0.04F;
    private static final float WOBBLE_X_RATE = 0.05F;
    private static final float WOBBLE_Z_RATE = 0.1F;

    private final ModelPart core;

    public TaintSporeSwarmerCoreLayer(RenderLayerParent<TaintSporeRenderState, TaintSporeSwarmerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.core = modelSet.bakeLayer(TCModelLayers.TAINT_SPORE_SWARMER_CORE).getChild("core");
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, TaintSporeRenderState state, float yRot, float xRot) {
        core.resetPose();
        float wobble = state.hasRedOverlay ? HURT_WOBBLE : CALM_WOBBLE;
        core.xRot = wobble * Mth.sin(state.ageInTicks * WOBBLE_X_RATE);
        core.zRot = wobble * Mth.sin(state.ageInTicks * WOBBLE_Z_RATE);
        float size = SIZE_SCALE * state.displaySize;
        float pulse = PULSE_SCALE * Mth.sin(state.ageInTicks * PULSE_RATE);
        float horizontal = Math.max(MIN_SCALE, size + pulse);
        float vertical = Math.max(MIN_SCALE, size - pulse);
        poseStack.pushPose();
        poseStack.translate(0.0F, LEGACY_LIFT, 0.0F);
        poseStack.scale(horizontal, vertical, horizontal);
        poseStack.translate(0.0F, -vertical / 2.0F, 0.0F);
        collector.order(1).submitModelPart(core, poseStack, RenderTypes.entityTranslucentEmissive(AbstractTaintSporeRenderer.TEXTURE), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, -1,
                null);
        poseStack.popPose();
    }
}
