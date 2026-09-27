package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintChicken;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.chicken.AdultChickenModel;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class TaintChickenRenderer extends MobRenderer<EntityTaintChicken, ChickenRenderState, ChickenModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/taint_chicken.png");
    private static final float SHADOW = 0.3F;

    public TaintChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), SHADOW);
    }

    @Override
    public Identifier getTextureLocation(ChickenRenderState state) {
        return TEXTURE;
    }

    @Override
    public ChickenRenderState createRenderState() {
        return new ChickenRenderState();
    }

    @Override
    public void extractRenderState(EntityTaintChicken entity, ChickenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flap = Mth.lerp(partialTicks, entity.oFlap, entity.flap);
        state.flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.flapSpeed);
    }

    @Override
    protected void scale(ChickenRenderState state, PoseStack poseStack) {
        AdultModelBabies.scale(state, poseStack);
    }
}
