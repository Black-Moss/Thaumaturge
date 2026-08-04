package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CrucibleRenderState extends BlockEntityRenderState {
    public FluidStack fluid;
    public float fluidHeight;
    public TextureAtlasSprite sprite;
    public int color;
}
