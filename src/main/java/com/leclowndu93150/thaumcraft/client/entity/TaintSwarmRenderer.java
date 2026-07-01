package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.entity.EntityTaintSwarm;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.Identifier;

public final class TaintSwarmRenderer extends MobRenderer<EntityTaintSwarm, PhantomRenderState, PhantomModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/entity/taint_swarm.png");

    public TaintSwarmRenderer(EntityRendererProvider.Context context) {
        super(context, new PhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(PhantomRenderState state) {
        return TEXTURE;
    }

    @Override
    public PhantomRenderState createRenderState() {
        return new PhantomRenderState();
    }
}
