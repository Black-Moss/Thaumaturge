package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public final class DioptraRenderState extends BlockEntityRenderState {
    public final byte[] grid = new byte[BlockEntityDioptra.GRID_LENGTH];
    public boolean enabled;
    public float time;
}
