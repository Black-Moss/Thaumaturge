package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.entity.ThaumicSlime;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

public final class ThaumicSlimeRenderer extends MobRenderer<ThaumicSlime, SlimeRenderState, SlimeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/entity/thaumic_slime.png");

    public ThaumicSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(ThaumicSlime entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.getSize();
        state.squish = entity.oSquish + (entity.squish - entity.oSquish) * partialTicks;
    }
}
