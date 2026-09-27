package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;

public final class TaintVillagerRenderer extends VillagerRenderer {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/taint_villager.png");

    public TaintVillagerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(Villager entity, VillagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        AdultModelBabies.useAdultModel(state);
    }

    @Override
    protected void scale(VillagerRenderState state, PoseStack poseStack) {
        AdultModelBabies.scale(state, poseStack);
    }
}
