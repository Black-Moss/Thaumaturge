package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class TCModelLayers {
    public static final ModelLayerLocation TAINTACLE = new ModelLayerLocation(TCIds.rl("taintacle"), "main");
    public static final ModelLayerLocation TAINTACLE_SMALL = new ModelLayerLocation(TCIds.rl("taintacle_small"), "main");
    public static final ModelLayerLocation TAINT_SEED = new ModelLayerLocation(TCIds.rl("taint_seed"), "main");

    private TCModelLayers() {}
}
