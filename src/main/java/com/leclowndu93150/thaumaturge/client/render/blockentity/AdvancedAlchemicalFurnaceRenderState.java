package com.leclowndu93150.thaumaturge.client.render.blockentity;

import java.util.Map;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public final class AdvancedAlchemicalFurnaceRenderState extends BlockEntityRenderState {
    public boolean assembled;
    public int heat;
    public int stored;
    public Map<String, int[][]> meshLights = Map.of();
}
