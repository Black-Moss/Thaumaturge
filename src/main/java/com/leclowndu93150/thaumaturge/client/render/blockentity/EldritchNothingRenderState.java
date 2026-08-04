package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class EldritchNothingRenderState extends BlockEntityRenderState {
    public final boolean[] exposed = new boolean[Direction.values().length];
    public boolean anyExposed;
}
