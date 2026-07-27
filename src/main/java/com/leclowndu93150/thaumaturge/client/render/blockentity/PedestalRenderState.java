package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public class PedestalRenderState extends BlockEntityRenderState {
    public @Nullable ItemStackRenderState item;
    public float spin;
    public float groundLift;
}
