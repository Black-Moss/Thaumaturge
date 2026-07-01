package com.leclowndu93150.thaumcraft.data.tag;

import com.leclowndu93150.thaumcraft.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumcraft.registry.TCTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class TCBiomeTagsProvider extends KeyTagProvider<Biome> {
    public TCBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BIOME, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TCTags.HAS_GREATWOOD)
                .add(Biomes.FOREST)
                .add(Biomes.FLOWER_FOREST)
                .add(Biomes.BIRCH_FOREST)
                .add(Biomes.OLD_GROWTH_BIRCH_FOREST)
                .add(Biomes.DARK_FOREST)
                .add(TCBiomes.MAGICAL_FOREST);
        tag(TCTags.HAS_GREATWOOD_RARE)
                .add(Biomes.TAIGA)
                .add(Biomes.OLD_GROWTH_PINE_TAIGA)
                .add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
                .add(Biomes.SAVANNA)
                .add(Biomes.SAVANNA_PLATEAU)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.SWAMP)
                .add(Biomes.MANGROVE_SWAMP);
        tag(TCTags.HAS_SILVERWOOD)
                .add(TCBiomes.MAGICAL_FOREST);
        tag(TCTags.HAS_CINDERPEARL)
                .add(Biomes.DESERT)
                .add(Biomes.BADLANDS)
                .add(Biomes.ERODED_BADLANDS)
                .add(Biomes.WOODED_BADLANDS);
        tag(BiomeTags.IS_OVERWORLD)
                .add(TCBiomes.MAGICAL_FOREST)
                .add(TCBiomes.EERIE)
                .add(TCBiomes.ELDRITCH);
        tag(BiomeTags.IS_FOREST)
                .add(TCBiomes.MAGICAL_FOREST);
    }
}
