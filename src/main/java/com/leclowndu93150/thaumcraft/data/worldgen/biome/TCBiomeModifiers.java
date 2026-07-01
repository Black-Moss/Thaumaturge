package com.leclowndu93150.thaumcraft.data.worldgen.biome;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.data.worldgen.feature.TCPlacedFeatures;
import com.leclowndu93150.thaumcraft.registry.TCTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TCBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_ORES = key("add_ores");
    public static final ResourceKey<BiomeModifier> ADD_CRYSTALS = key("add_crystals");
    public static final ResourceKey<BiomeModifier> ADD_GREATWOOD = key("add_greatwood");
    public static final ResourceKey<BiomeModifier> ADD_GREATWOOD_RARE = key("add_greatwood_rare");
    public static final ResourceKey<BiomeModifier> ADD_SILVERWOOD = key("add_silverwood");
    public static final ResourceKey<BiomeModifier> ADD_CINDERPEARL = key("add_cinderpearl");

    private TCBiomeModifiers() {}

    private static ResourceKey<BiomeModifier> key(String path) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TCIds.rl(path));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);

        context.register(ADD_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(
                        features.getOrThrow(TCPlacedFeatures.ORE_CINNABAR),
                        features.getOrThrow(TCPlacedFeatures.ORE_QUARTZ),
                        features.getOrThrow(TCPlacedFeatures.ORE_AMBER)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_CRYSTALS, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(features.getOrThrow(TCPlacedFeatures.CRYSTALS)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_GREATWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(TCTags.HAS_GREATWOOD),
                HolderSet.direct(features.getOrThrow(TCPlacedFeatures.GREATWOOD_NATURAL)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_GREATWOOD_RARE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(TCTags.HAS_GREATWOOD_RARE),
                HolderSet.direct(features.getOrThrow(TCPlacedFeatures.GREATWOOD_NATURAL_RARE)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_SILVERWOOD, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(TCTags.HAS_SILVERWOOD),
                HolderSet.direct(features.getOrThrow(TCPlacedFeatures.SILVERWOOD_NATURAL)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_CINDERPEARL, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(TCTags.HAS_CINDERPEARL),
                HolderSet.direct(features.getOrThrow(TCPlacedFeatures.CINDERPEARL)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }
}
