package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class FluxScrubberRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.DOWN;
    public float bob;
}
