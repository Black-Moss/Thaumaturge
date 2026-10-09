package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintacleModel;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintacle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class TaintacleRenderer extends MobRenderer<AbstractTaintacle, TaintacleRenderState, TaintacleModel> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/taintacle.png");
    private static final float REFERENCE_HEIGHT = 3.0F;
    private static final float EMERGE_TICKS_PER_HEIGHT = 10.0F;

    public TaintacleRenderer(EntityRendererProvider.Context context, float shadowRadius) {
        super(context, new TaintacleModel(context.bakeLayer(TTModelLayers.TAINTACLE)), shadowRadius);
    }

    @Override
    public Identifier getTextureLocation(TaintacleRenderState state) {
        return TEXTURE;
    }

    @Override
    public TaintacleRenderState createRenderState() {
        return new TaintacleRenderState();
    }

    @Override
    public void extractRenderState(AbstractTaintacle entity, TaintacleRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.emergence = emergenceOf(state.ageInTicks, state.boundingBoxHeight);
        state.strikeTime = remaining(entity.strikeTicks(), partialTicks);
        state.hurt = remaining(entity.hurtTime, partialTicks);
        state.enrage = entity.enrage();
        state.flail = entity.flailIntensity;
    }

    @Override
    protected void scale(TaintacleRenderState state, PoseStack poseStack) {
        float uniform = state.boundingBoxHeight / REFERENCE_HEIGHT;
        poseStack.scale(uniform, uniform, uniform);
    }

    private static float remaining(float ticks, float partialTicks) {
        return Math.max(0.0F, ticks - partialTicks);
    }

    private static float emergenceOf(float age, float height) {
        return Mth.clamp(age / (height * EMERGE_TICKS_PER_HEIGHT), 0.0F, 1.0F);
    }
}
