package com.leclowndu93150.thaumcraft.data;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.compat.curio.data.TCCurioProvider;
import com.leclowndu93150.thaumcraft.data.damagetype.TCDamageTypeBootstrap;
import com.leclowndu93150.thaumcraft.data.datamap.*;
import com.leclowndu93150.thaumcraft.data.lang.TCEnglishProvider;
import com.leclowndu93150.thaumcraft.data.loot.TCBlockLootSubProvider;
import com.leclowndu93150.thaumcraft.data.loot.TCEntityLootSubProvider;
import com.leclowndu93150.thaumcraft.data.loot.TCGameplayLootSubProvider;
import com.leclowndu93150.thaumcraft.data.loot.TCGlobalLootModifierProvider;
import com.leclowndu93150.thaumcraft.data.model.TCModelProvider;
import com.leclowndu93150.thaumcraft.data.recipe.TCRecipeProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCBiomeTagsProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCEntityTypeTagsProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCBlockTagsProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCDamageTypeTagsProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCItemTagsProvider;
import com.leclowndu93150.thaumcraft.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumcraft.data.worldgen.biome.TCBiomeModifiers;
import com.leclowndu93150.thaumcraft.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumcraft.data.worldgen.dimension.OuterLandsBootstrap;
import com.leclowndu93150.thaumcraft.data.worldgen.feature.TCConfiguredFeatures;
import com.leclowndu93150.thaumcraft.data.worldgen.feature.TCPlacedFeatures;
import com.leclowndu93150.thaumcraft.data.worldgen.feature.TCStructureBootstrap;
import com.leclowndu93150.thaumcraft.data.worldgen.research.CategoryBootstrap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCDataGenerators {
    private TCDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        RegistrySetBuilder registries = new RegistrySetBuilder()
                .add(IAspect.REGISTRY_KEY, AspectBootstrap::bootstrap)
                .add(IResearchCategory.REGISTRY_KEY, CategoryBootstrap::bootstrap)
                .add(Registries.DAMAGE_TYPE, TCDamageTypeBootstrap::bootstrap)
                .add(Registries.CONFIGURED_FEATURE, TCConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, TCPlacedFeatures::bootstrap)
                .add(Registries.BIOME, TCBiomes::bootstrap)
                .add(Registries.DIMENSION_TYPE, OuterLandsBootstrap::bootstrapTypes)
                .add(Registries.LEVEL_STEM, OuterLandsBootstrap::bootstrapStems)
                .add(Registries.STRUCTURE, TCStructureBootstrap::bootstrapStructures)
                .add(Registries.STRUCTURE_SET, TCStructureBootstrap::bootstrapSets)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TCBiomeModifiers::bootstrap);
        event.createDatapackRegistryObjects(registries);

        event.createProvider(TCEnglishProvider::new);
        event.createProvider(TCModelProvider::new);
        event.createProvider(TCRecipeProvider.Runner::new);
        event.createProvider(AuraModifierProvider::new);
        event.createProvider(EntityAspectsProvider::new);
        event.createProvider(ChampionWhitelistProvider::new);
        event.createProvider(InfernalBonusProvider::new);
        event.createProvider(StrippingProvider::new);
        event.createProvider(TCCurioProvider::new);

        event.createBlockAndItemTags(TCBlockTagsProvider::new, TCItemTagsProvider::new);
        event.createProvider(TCDamageTypeTagsProvider::new);
        event.createProvider(TCBiomeTagsProvider::new);
        event.createProvider(TCEntityTypeTagsProvider::new);

        event.createProvider((output, lookupProvider) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(
                                TCBlockLootSubProvider::new,
                                LootContextParamSets.BLOCK
                        ),
                        new LootTableProvider.SubProviderEntry(
                                TCEntityLootSubProvider::new,
                                LootContextParamSets.ENTITY
                        ),
                        new LootTableProvider.SubProviderEntry(
                                TCGameplayLootSubProvider::new,
                                LootContextParamSets.CHEST
                        )),
                lookupProvider
        ));

        event.createProvider(TCGlobalLootModifierProvider::new);
    }
}
