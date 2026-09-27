package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintPig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public final class TaintPigRenderer extends MobRenderer<EntityTaintPig, LivingEntityRenderState, PigModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/taint_pig.png");
    private static final float SHADOW = 0.7F;

    public TaintPigRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), SHADOW);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        AdultModelBabies.scale(state, poseStack);
    }
}
