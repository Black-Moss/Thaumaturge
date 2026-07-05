package com.leclowndu93150.thaumcraft.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;

public final class InfusionMatrixRenderState extends BlockEntityRenderState {
    public float animationTime;
    public float startUp;
    public float stability;
    public int craftTicks;
    public boolean active;
    public boolean crafting;
    public boolean fancyGraphics;
    public Identifier texture;
}
