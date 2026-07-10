package com.leclowndu93150.thaumcraft.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class MirrorRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.UP;
    public boolean linked;
    public boolean inRange;
    public final List<BlockStateModelPart> frameParts = new ArrayList<>();
}
