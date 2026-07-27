package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jspecify.annotations.Nullable;

public final class EldritchCapRenderState extends BlockEntityRenderState {
    public int eyes;
    public boolean outerLands;
    public @Nullable ItemStackRenderState eye;
}
