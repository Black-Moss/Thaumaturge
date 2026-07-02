package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class CrucibleRenderState extends BlockEntityRenderState {
    public FluidStack fluid;
    public float fluidHeight;
    public TextureAtlasSprite sprite;
    public int color;
}
