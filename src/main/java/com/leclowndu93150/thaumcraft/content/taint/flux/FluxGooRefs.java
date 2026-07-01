package com.leclowndu93150.thaumcraft.content.taint.flux;

import com.leclowndu93150.thaumcraft.registry.TCFluids;
import net.minecraft.world.level.material.FlowingFluid;

public final class FluxGooRefs {
    private FluxGooRefs() {}

    public static FlowingFluid sourceFluid() {
        return TCFluids.FLUX_GOO_SOURCE.get();
    }
}
