package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.registry.TTFluids;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;

public final class TTFluidTagsProvider extends KeyTagProvider<Fluid> {
    public TTFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.FLUID, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(FluidTags.WATER).add(TTFluids.PURIFYING_SOURCE.getKey()).add(TTFluids.PURIFYING_FLOWING.getKey());
    }
}
