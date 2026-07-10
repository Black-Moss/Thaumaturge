package com.leclowndu93150.thaumcraft.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class PatternCrafterRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public byte patternType;
    public float rotation;
}
