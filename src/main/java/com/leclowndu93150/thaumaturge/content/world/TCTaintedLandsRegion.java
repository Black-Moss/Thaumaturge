package com.leclowndu93150.thaumaturge.content.world;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

public final class TCTaintedLandsRegion extends Region {
    public TCTaintedLandsRegion(Identifier name) {
        super(name, RegionType.OVERWORLD, ThaumaturgeCommonConfig.TAINTED_LANDS_REGION_WEIGHT.get());
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(Biomes.FOREST, TCBiomes.TAINTED_LANDS);
            builder.replaceBiome(Biomes.FLOWER_FOREST, TCBiomes.TAINTED_LANDS);
            builder.replaceBiome(Biomes.BEACH, TCBiomes.TAINTED_LANDS);
        });
    }
}
