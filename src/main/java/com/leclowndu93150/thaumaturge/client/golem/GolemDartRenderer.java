package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.content.entity.EntityGolemDart;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public final class GolemDartRenderer extends ArrowRenderer<EntityGolemDart, ArrowRenderState> {
    public GolemDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TippableArrowRenderer.NORMAL_ARROW_LOCATION;
    }
}
