package com.leclowndu93150.thaumcraft.data.tag;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCBlockTags;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.concurrent.CompletableFuture;

public final class TCBlockTagsProvider extends BlockTagsProvider {
    public TCBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TCIds.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TCBlockTags.LAMP_GROWTH_BLACKLIST);
        tag(TCBlockTags.INFUSION_STABILISERS)
                .add(Blocks.SKELETON_SKULL)
                .add(Blocks.SKELETON_WALL_SKULL)
                .add(Blocks.WITHER_SKELETON_SKULL)
                .add(Blocks.WITHER_SKELETON_WALL_SKULL)
                .add(Blocks.ZOMBIE_HEAD)
                .add(Blocks.ZOMBIE_WALL_HEAD)
                .add(Blocks.PLAYER_HEAD)
                .add(Blocks.PLAYER_WALL_HEAD)
                .add(Blocks.CREEPER_HEAD)
                .add(Blocks.CREEPER_WALL_HEAD)
                .add(Blocks.DRAGON_HEAD)
                .add(Blocks.DRAGON_WALL_HEAD)
                .add(Blocks.PIGLIN_HEAD)
                .add(Blocks.PIGLIN_WALL_HEAD);

        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(TCBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocks.METAL_BRASS_BLOCK.get())
                .add(TCBlocks.METAL_VOID_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(TCBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocks.METAL_BRASS_BLOCK.get())
                .add(TCBlocks.METAL_VOID_BLOCK.get())
                .add(TCBlocks.OBSIDIAN_PLACEHOLDER.get())
                .add(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get())
                .add(TCBlocks.INFERNAL_FURNACE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(TCBlocks.METAL_THAUMIUM_BLOCK.get())
                .add(TCBlocks.METAL_BRASS_BLOCK.get())
                .add(TCBlocks.METAL_VOID_BLOCK.get());

        tag(BlockTags.LOGS_THAT_BURN)
                .add(TCBlocks.LOG_GREATWOOD.get())
                .add(TCBlocks.LOG_SILVERWOOD.get());

        tag(BlockTags.LEAVES)
                .add(TCBlocks.LEAVES_GREATWOOD.get())
                .add(TCBlocks.LEAVES_SILVERWOOD.get());

        tag(BlockTags.SAPLINGS)
                .add(TCBlocks.SAPLING_GREATWOOD.get())
                .add(TCBlocks.SAPLING_SILVERWOOD.get());

        tag(BlockTags.PLANKS)
                .add(TCBlocks.PLANK_GREATWOOD.get())
                .add(TCBlocks.PLANK_SILVERWOOD.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(TCBlocks.LOG_GREATWOOD.get())
                .add(TCBlocks.LOG_SILVERWOOD.get())
                .add(TCBlocks.PLANK_GREATWOOD.get())
                .add(TCBlocks.PLANK_SILVERWOOD.get());

        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(TCBlocks.LEAVES_GREATWOOD.get())
                .add(TCBlocks.LEAVES_SILVERWOOD.get());

        tag(TCBlockTags.CRUCIBLE_HEAT_SOURCES)
                .add(Blocks.LAVA)
                .add(Blocks.FIRE)
                .add(Blocks.CAMPFIRE)
                .add(Blocks.SOUL_FIRE)
                .add(Blocks.SOUL_CAMPFIRE)
                .add(Blocks.MAGMA_BLOCK)
                .addAll(TCBlocks.NITORS.values().stream().map(DeferredHolder::get));

        tag(TCBlockTags.ORES_AMBER).add(TCBlocks.ORE_AMBER.get());
        tag(TCBlockTags.ORES_CINNABAR).add(TCBlocks.ORE_CINNABAR.get());
        tag(Tags.Blocks.ORES_QUARTZ).add(TCBlocks.ORE_QUARTZ.get());
        tag(Tags.Blocks.ORES).addTags(TCBlockTags.ORES_AMBER,TCBlockTags.ORES_CINNABAR);

        tag(TCBlockTags.PORTABLE_HOLE_BLACKLIST);

        tag(TCBlockTags.STORAGE_BLOCKS_AMBER).add(TCBlocks.AMBER_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_BRASS).add(TCBlocks.METAL_BRASS_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_THAUMIUM).add(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        tag(TCBlockTags.STORAGE_BLOCKS_VOID_METAL).add(TCBlocks.METAL_VOID_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTags(TCBlockTags.STORAGE_BLOCKS_AMBER,TCBlockTags.STORAGE_BLOCKS_BRASS,TCBlockTags.STORAGE_BLOCKS_THAUMIUM,TCBlockTags.STORAGE_BLOCKS_VOID_METAL);

    }
}
