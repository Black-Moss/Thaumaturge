package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public final class TCLootTables {
    public static final ResourceKey<LootTable> LOOT_BAG_COMMON = key("gameplay/loot_bag_common");
    public static final ResourceKey<LootTable> LOOT_BAG_UNCOMMON = key("gameplay/loot_bag_uncommon");
    public static final ResourceKey<LootTable> LOOT_BAG_RARE = key("gameplay/loot_bag_rare");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, TCIds.rl(path));
    }

    private TCLootTables() {}
}
