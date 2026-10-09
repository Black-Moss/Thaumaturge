package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeModel;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSpore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class TaintSporeRenderer extends AbstractTaintSporeRenderer<EntityTaintSpore, TaintSporeModel> {
    private static final float SIZE_FACTOR = 0.12F;
    private static final float PULSE_AMPLITUDE = 0.025F;
    private static final float PULSE_RATE = 0.075F;
    private static final float MIN_SCALE = 0.01F;

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeModel(context.bakeLayer(TTModelLayers.TAINT_SPORE)));
    }

    @Override
    protected void scale(TaintSporeRenderState state, PoseStack poseStack) {
        float base = SIZE_FACTOR * state.displaySize;
        float pulse = PULSE_AMPLITUDE * Mth.sin(PULSE_RATE * state.ageInTicks);
        float horizontal = Math.max(MIN_SCALE, base + pulse);
        float vertical = Math.max(MIN_SCALE, base - pulse);
        poseStack.scale(horizontal, vertical, horizontal);
    }
}
