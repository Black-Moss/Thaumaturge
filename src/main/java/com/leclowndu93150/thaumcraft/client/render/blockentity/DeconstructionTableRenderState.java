package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState book;
    public @Nullable ItemStackRenderState input;
    public @Nullable Holder<IAspect> aspect;
    public float ticks;
}
