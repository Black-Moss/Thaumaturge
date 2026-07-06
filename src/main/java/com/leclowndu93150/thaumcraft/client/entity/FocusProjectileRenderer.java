package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.content.entity.EntityFocusProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class FocusProjectileRenderer extends EntityRenderer<EntityFocusProjectile, EntityRenderState> {
    private static final float SHADOW = 0.1F;

    public FocusProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(EntityFocusProjectile entity, EntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        entity.renderParticle(partialTicks);
    }
}
