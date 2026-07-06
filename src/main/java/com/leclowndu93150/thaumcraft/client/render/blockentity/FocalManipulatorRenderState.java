package com.leclowndu93150.thaumcraft.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public final class FocalManipulatorRenderState extends BlockEntityRenderState {
    public float ticks;
    public @Nullable ItemStackRenderState focus;
    public float focusLift;
    public float crystalLift;
    public final List<ItemStackRenderState> crystals = new ArrayList<>();
    public final List<Integer> crystalColors = new ArrayList<>();
}
