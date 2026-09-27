package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.sheep.Sheep;

public final class TaintSheepRenderer extends SheepRenderer {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/taint_sheep.png");

    public TaintSheepRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(SheepRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(Sheep entity, SheepRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        AdultModelBabies.useAdultModel(state);
    }

    @Override
    protected void scale(SheepRenderState state, PoseStack poseStack) {
        AdultModelBabies.scale(state, poseStack);
    }
}
