package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.content.device.BlockEntityDioptra;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public final class DioptraRenderState extends BlockEntityRenderState {
    public final byte[] grid = new byte[BlockEntityDioptra.GRID_LENGTH];
    public boolean enabled;
    public float time;
}
