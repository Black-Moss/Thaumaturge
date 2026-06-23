package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class TCItemTags {
    public static final TagKey<Item> WANDS = key("wands");

    private TCItemTags() {}

    private static TagKey<Item> key(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TCIds.MODID, path));
    }
}
