package com.leclowndu93150.thaumcraft.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public final class PedestalRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState item;
    public float spin;
    public float groundLift;
}
