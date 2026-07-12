package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BellowsRenderState extends BlockEntityRenderState {

    public boolean extension;
    public Direction facing = Direction.NORTH;
    public float scale = 1f;
    public List<BlockStateModelPart>[] parts = new List[3];
    public BlockStateModel[] models = new BlockStateModel[3];
    public long seed;

}
