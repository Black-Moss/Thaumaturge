package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class ResearchTableRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean hasTools;
    public boolean hasNote;
    public int noteColor;
}
