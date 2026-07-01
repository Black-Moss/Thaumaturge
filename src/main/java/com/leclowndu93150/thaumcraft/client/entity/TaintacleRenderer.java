package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.entity.AbstractTaintacle;
import net.minecraft.client.model.monster.hoglin.HoglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.resources.Identifier;

public final class TaintacleRenderer extends MobRenderer<AbstractTaintacle, HoglinRenderState, HoglinModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/entity/taintacle.png");

    public TaintacleRenderer(EntityRendererProvider.Context context) {
        super(context, new HoglinModel(context.bakeLayer(ModelLayers.HOGLIN)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState state) {
        return TEXTURE;
    }

    @Override
    public HoglinRenderState createRenderState() {
        return new HoglinRenderState();
    }
}
