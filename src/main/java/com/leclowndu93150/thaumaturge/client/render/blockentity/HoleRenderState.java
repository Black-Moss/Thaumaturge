package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HoleRenderState extends BlockEntityRenderState {
    public VoxelShape shape = Shapes.empty();
    public final boolean[] hideBoundary = new boolean[Direction.values().length];
}
