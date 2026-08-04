package com.leclowndu93150.thaumaturge.client.render.blockentity;

import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class BellowsRenderState extends BlockEntityRenderState {

    public boolean extension;
    public Direction facing = Direction.NORTH;
    public float scale = 1f;
    public List<BlockStateModelPart>[] parts = new List[3];
    public BlockStateModel[] models = new BlockStateModel[3];
    public long seed;
}
