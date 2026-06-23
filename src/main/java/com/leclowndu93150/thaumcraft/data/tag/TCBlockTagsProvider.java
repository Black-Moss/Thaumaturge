package com.leclowndu93150.thaumcraft.data.tag;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksFMetals;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public final class TCBlockTagsProvider extends BlockTagsProvider {
    public TCBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TCIds.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(TCBlocksFMetals.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocksFMetals.METAL_BRASS_BLOCK.get())
                .add(TCBlocksFMetals.METAL_VOID_BLOCK.get())
                .add(TCBlocksFMetals.METAL_INFUSED_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocksFMetals.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocksFMetals.METAL_BRASS_BLOCK.get())
                .add(TCBlocksFMetals.METAL_VOID_BLOCK.get())
                .add(TCBlocksFMetals.METAL_INFUSED_BLOCK.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(TCBlocksFMetals.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocksFMetals.METAL_BRASS_BLOCK.get())
                .add(TCBlocksFMetals.METAL_VOID_BLOCK.get())
                .add(TCBlocksFMetals.METAL_INFUSED_BLOCK.get());
    }
}
