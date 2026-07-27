package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Strippable;

import java.util.concurrent.CompletableFuture;

public final class StrippingProvider extends DataMapProvider {
    public StrippingProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<Strippable, Block> b = builder(NeoForgeDataMaps.STRIPPABLES);

        b.add(TCBlocks.LOG_GREATWOOD,new Strippable(TCBlocks.STRIPPED_LOG_GREATWOOD.get()),false);
        b.add(TCBlocks.WOOD_GREATWOOD,new Strippable(TCBlocks.STRIPPED_WOOD_GREATWOOD.get()),false);
        b.add(TCBlocks.LOG_SILVERWOOD,new Strippable(TCBlocks.STRIPPED_LOG_SILVERWOOD.get()),false);
        b.add(TCBlocks.WOOD_SILVERWOOD,new Strippable(TCBlocks.STRIPPED_WOOD_SILVERWOOD.get()),false);
    }

    @Override
    public String getName() {
        return "Stripping Data Map";
    }

}
