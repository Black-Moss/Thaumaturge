package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class TCBlockTags {
    public static final TagKey<Block> CRUCIBLE_HEAT_SOURCES = key("crucible_heat_sources");

    public static final TagKey<Block> ORES_AMBER = common("ores/amber");
    public static final TagKey<Block> ORES_CINNABAR = common("ores/cinnabar");

    public static final TagKey<Block> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
    public static final TagKey<Block> STORAGE_BLOCKS_THAUMIUM = common("storage_blocks/thaumium");
    public static final TagKey<Block> STORAGE_BLOCKS_VOID_METAL = common("storage_blocks/void_metal");
    public static final TagKey<Block> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");

    public static final TagKey<Block> INFUSION_STABILISERS = key("infusion_stabilisers");

    private TCBlockTags() {}

    private static TagKey<Block> key(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TCIds.MODID, path));
    }

    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
    }
}
