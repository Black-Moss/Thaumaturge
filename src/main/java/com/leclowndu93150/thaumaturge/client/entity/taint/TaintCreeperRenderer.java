package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public final class TaintCreeperRenderer extends CreeperRenderer {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/taint_creeper.png");

    public TaintCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}
