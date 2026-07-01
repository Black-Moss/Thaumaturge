package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.entity.AbstractTaintSeed;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;

public final class TaintSeedRenderer extends MobRenderer<AbstractTaintSeed, GhastRenderState, GhastModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/entity/taint_seed.png");

    public TaintSeedRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel(context.bakeLayer(ModelLayers.GHAST)), 0.7F);
    }

    @Override
    public Identifier getTextureLocation(GhastRenderState state) {
        return TEXTURE;
    }

    @Override
    public GhastRenderState createRenderState() {
        return new GhastRenderState();
    }
}
