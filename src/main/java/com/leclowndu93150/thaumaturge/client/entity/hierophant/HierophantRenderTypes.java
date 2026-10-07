package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class HierophantRenderTypes {
    public static final RenderType EYE = RenderTypes.eyes(HierophantTextures.BOSS);
    public static final RenderType SPELL = TTFXRenderTypes.translucent(HierophantTextures.SPELLS);
    public static final RenderType ARC = TTFXRenderTypes.additive(HierophantTextures.ARC);
    public static final RenderType HAMMER = RenderTypes.entityCutout(HierophantTextures.BOSS);
    private HierophantRenderTypes() {}
}
