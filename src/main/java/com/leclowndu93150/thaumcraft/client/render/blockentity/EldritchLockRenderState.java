package com.leclowndu93150.thaumcraft.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class EldritchLockRenderState extends BlockEntityRenderState {
    public int count;
    public float animationTime;
    public Direction facing = Direction.NORTH;
    public @Nullable ItemStackRenderState tablet;
}
