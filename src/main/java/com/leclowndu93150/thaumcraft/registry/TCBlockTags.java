package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class TCBlockTags {
    public static final TagKey<Block> CRUCIBLE_HEAT_SOURCES = key("crucible_heat_sources");

    private TCBlockTags() {}

    private static TagKey<Block> key(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TCIds.MODID, path));
    }
}
