package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.model.entity.FireBatModel;
import com.leclowndu93150.thaumcraft.content.entity.EntityFireBat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public final class FireBatRenderer extends MobRenderer<EntityFireBat, FireBatRenderState, FireBatModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/firebat.png");
    private static final float SHADOW = 0.25F;
    private static final float SCALE = 0.35F;
    private static final int FULLBRIGHT_BLOCK_LIGHT = 15;

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(TCModelLayers.FIRE_BAT)), SHADOW);
    }

    @Override
    public FireBatRenderState createRenderState() {
        return new FireBatRenderState();
    }

    @Override
    public void extractRenderState(EntityFireBat entity, FireBatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hanging = entity.isHanging();
    }

    @Override
    protected int getBlockLightLevel(EntityFireBat entity, BlockPos pos) {
        return FULLBRIGHT_BLOCK_LIGHT;
    }

    @Override
    public Identifier getTextureLocation(FireBatRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(FireBatRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
