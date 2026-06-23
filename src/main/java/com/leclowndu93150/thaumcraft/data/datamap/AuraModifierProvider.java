package com.leclowndu93150.thaumcraft.data.datamap;

import com.leclowndu93150.thaumcraft.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumcraft.registry.TCDataMaps;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class AuraModifierProvider extends DataMapProvider {
    public AuraModifierProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<BiomeAuraModifier, Biome> b = builder(TCDataMaps.BIOME_AURA_MODIFIER);

        add(b, Biomes.PLAINS, 0.3F);
        add(b, Biomes.SUNFLOWER_PLAINS, 0.3F);
        add(b, Biomes.SNOWY_PLAINS, 0.275F);
        add(b, Biomes.ICE_SPIKES, 0.25F);
        add(b, Biomes.DESERT, 0.25F);
        add(b, Biomes.SWAMP, 0.5F);
        add(b, Biomes.MANGROVE_SWAMP, 0.55F);
        add(b, Biomes.FOREST, 0.5F);
        add(b, Biomes.FLOWER_FOREST, 0.5F);
        add(b, Biomes.BIRCH_FOREST, 0.5F);
        add(b, Biomes.DARK_FOREST, 0.5F);
        add(b, Biomes.PALE_GARDEN, 0.6F);
        add(b, Biomes.OLD_GROWTH_BIRCH_FOREST, 0.5F);
        add(b, Biomes.OLD_GROWTH_PINE_TAIGA, 0.4F);
        add(b, Biomes.OLD_GROWTH_SPRUCE_TAIGA, 0.4F);
        add(b, Biomes.TAIGA, 0.4F);
        add(b, Biomes.SNOWY_TAIGA, 0.35F);
        add(b, Biomes.SAVANNA, 0.25F);
        add(b, Biomes.SAVANNA_PLATEAU, 0.25F);
        add(b, Biomes.WINDSWEPT_HILLS, 0.3F);
        add(b, Biomes.WINDSWEPT_GRAVELLY_HILLS, 0.3F);
        add(b, Biomes.WINDSWEPT_FOREST, 0.4F);
        add(b, Biomes.WINDSWEPT_SAVANNA, 0.275F);
        add(b, Biomes.JUNGLE, 0.6F);
        add(b, Biomes.SPARSE_JUNGLE, 0.4F);
        add(b, Biomes.BAMBOO_JUNGLE, 0.65F);
        add(b, Biomes.BADLANDS, 0.33F);
        add(b, Biomes.ERODED_BADLANDS, 0.33F);
        add(b, Biomes.WOODED_BADLANDS, 0.4F);
        add(b, Biomes.MEADOW, 0.4F);
        add(b, Biomes.CHERRY_GROVE, 0.6F);
        add(b, Biomes.GROVE, 0.4F);
        add(b, Biomes.SNOWY_SLOPES, 0.275F);
        add(b, Biomes.FROZEN_PEAKS, 0.275F);
        add(b, Biomes.JAGGED_PEAKS, 0.3F);
        add(b, Biomes.STONY_PEAKS, 0.3F);
        add(b, Biomes.RIVER, 0.4F);
        add(b, Biomes.FROZEN_RIVER, 0.35F);
        add(b, Biomes.BEACH, 0.3F);
        add(b, Biomes.SNOWY_BEACH, 0.275F);
        add(b, Biomes.STONY_SHORE, 0.3F);
        add(b, Biomes.WARM_OCEAN, 0.33F);
        add(b, Biomes.LUKEWARM_OCEAN, 0.33F);
        add(b, Biomes.DEEP_LUKEWARM_OCEAN, 0.33F);
        add(b, Biomes.OCEAN, 0.33F);
        add(b, Biomes.DEEP_OCEAN, 0.33F);
        add(b, Biomes.COLD_OCEAN, 0.33F);
        add(b, Biomes.DEEP_COLD_OCEAN, 0.33F);
        add(b, Biomes.FROZEN_OCEAN, 0.3F);
        add(b, Biomes.DEEP_FROZEN_OCEAN, 0.3F);
        add(b, Biomes.MUSHROOM_FIELDS, 0.75F);
        add(b, Biomes.DRIPSTONE_CAVES, 0.3F);
        add(b, Biomes.LUSH_CAVES, 0.5F);
        add(b, Biomes.DEEP_DARK, 0.1F);
        add(b, Biomes.NETHER_WASTES, 0.125F);
        add(b, Biomes.WARPED_FOREST, 0.125F);
        add(b, Biomes.CRIMSON_FOREST, 0.125F);
        add(b, Biomes.SOUL_SAND_VALLEY, 0.125F);
        add(b, Biomes.BASALT_DELTAS, 0.125F);
        add(b, Biomes.THE_END, 0.125F);
        add(b, Biomes.END_HIGHLANDS, 0.125F);
        add(b, Biomes.END_MIDLANDS, 0.125F);
        add(b, Biomes.SMALL_END_ISLANDS, 0.125F);
        add(b, Biomes.END_BARRENS, 0.125F);
        add(b, Biomes.THE_VOID, 0.0F);
    }

    private static void add(Builder<BiomeAuraModifier, Biome> b, ResourceKey<Biome> key, float value) {
        b.add(key, new BiomeAuraModifier(value), false);
    }
}
