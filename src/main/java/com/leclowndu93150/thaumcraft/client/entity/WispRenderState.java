package com.leclowndu93150.thaumcraft.client.entity;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class WispRenderState extends EntityRenderState {
    public int tick;
    public int color;
    public boolean dead;
}
