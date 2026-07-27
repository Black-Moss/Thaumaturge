package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class AlembicRenderState extends BlockEntityRenderState {

    public boolean hasFilter;
    public Direction facing = Direction.NORTH;
    public @Nullable Identifier filterTexture;
    public int filterColor;
    public @Nullable Holder<IAspect> filterAspect;

    public Direction[] connectedDirections = new Direction[0];
}
