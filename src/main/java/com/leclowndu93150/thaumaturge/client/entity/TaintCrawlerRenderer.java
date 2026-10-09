package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public final class TaintCrawlerRenderer extends MobRenderer<EntityTaintCrawler, LivingEntityRenderState, SilverfishModel> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/taint_crawler.png");
    private static final float CRAWLER_SCALE = 0.7F;

    public TaintCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    protected void scale(LivingEntityRenderState renderState, PoseStack poseStack) {
        poseStack.scale(CRAWLER_SCALE, CRAWLER_SCALE, CRAWLER_SCALE);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState renderState) {
        return TEXTURE;
    }
}
