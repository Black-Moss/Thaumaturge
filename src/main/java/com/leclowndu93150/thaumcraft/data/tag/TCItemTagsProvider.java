package com.leclowndu93150.thaumcraft.data.tag;

import com.leclowndu93150.thaumcraft.TCIds;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

public final class TCItemTagsProvider extends BlockTagCopyingItemTagProvider {
    public TCItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags
    ) {
        super(output, lookupProvider, blockTags, TCIds.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {}
}
