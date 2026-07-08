package com.leclowndu93150.thaumcraft.data.loot;

import com.leclowndu93150.thaumcraft.registry.TCLootTables;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TCGameplayLootSubProvider implements LootTableSubProvider {
    private static final float BAG_MIN_ROLLS = 8.0F;
    private static final float BAG_MAX_ROLLS = 12.0F;

    private final HolderLookup.Provider registries;

    public TCGameplayLootSubProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(TCLootTables.LOOT_BAG_COMMON, bagTable(TreasureLootPools.COMMON));
        output.accept(TCLootTables.LOOT_BAG_UNCOMMON, bagTable(TreasureLootPools.UNCOMMON));
        output.accept(TCLootTables.LOOT_BAG_RARE, bagTable(TreasureLootPools.RARE));
    }

    private LootTable.Builder bagTable(int rarity) {
        return LootTable.lootTable()
                .withPool(TreasureLootPools.treasurePool(registries, rarity,
                        UniformGenerator.between(BAG_MIN_ROLLS, BAG_MAX_ROLLS)));
    }
}
