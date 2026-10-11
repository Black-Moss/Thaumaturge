package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeModel;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSpore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public final class TaintSporeRenderer extends AbstractTaintSporeRenderer<EntityTaintSpore, TaintSporeModel> {
    private static final float MIN_SCALE = 0.01F;
    private static final float BASE_SCALE = 0.3F;
    private static final float SCALE_PER_SIZE = 0.075F;
    private static final float BREATH_DEPTH = 0.06F;
    private static final float BREATH_PERIOD_TICKS = 70.0F;

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeModel(context.bakeLayer(TTModelLayers.TAINT_SPORE)));
    }

    @Override
    protected void scale(TaintSporeRenderState state, PoseStack poseStack) {
        float size = Math.max(MIN_SCALE, BASE_SCALE + state.displaySize * SCALE_PER_SIZE);
        float breath = BREATH_DEPTH * Mth.sin(state.ageInTicks * Mth.TWO_PI / BREATH_PERIOD_TICKS);
        float across = Math.max(MIN_SCALE, size * (1.0F + breath));
        float tall = Math.max(MIN_SCALE, size * (1.0F - breath));
        poseStack.scale(across, tall, across);
    }
}
