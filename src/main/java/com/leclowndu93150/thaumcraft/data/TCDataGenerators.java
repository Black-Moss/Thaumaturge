package com.leclowndu93150.thaumcraft.data;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.data.damagetype.TCDamageTypeBootstrap;
import com.leclowndu93150.thaumcraft.data.datamap.AuraModifierProvider;
import com.leclowndu93150.thaumcraft.data.lang.TCEnglishProvider;
import com.leclowndu93150.thaumcraft.data.loot.TCBlockLootSubProvider;
import com.leclowndu93150.thaumcraft.data.model.TCModelProvider;
import com.leclowndu93150.thaumcraft.data.recipe.TCRecipeProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCBlockTagsProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCDamageTypeTagsProvider;
import com.leclowndu93150.thaumcraft.data.tag.TCItemTagsProvider;
import com.leclowndu93150.thaumcraft.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumcraft.data.worldgen.research.CategoryBootstrap;
import com.leclowndu93150.thaumcraft.data.worldgen.research.EntryBootstrap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCDataGenerators {
    private TCDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        RegistrySetBuilder registries = new RegistrySetBuilder()
                .add(IAspect.REGISTRY_KEY, AspectBootstrap::bootstrap)
                .add(IResearchCategory.REGISTRY_KEY, CategoryBootstrap::bootstrap)
                .add(IResearchEntry.REGISTRY_KEY, EntryBootstrap::bootstrap)
                .add(Registries.DAMAGE_TYPE, TCDamageTypeBootstrap::bootstrap);
        event.createDatapackRegistryObjects(registries);

        event.createProvider(TCEnglishProvider::new);
        event.createProvider(TCModelProvider::new);
        event.createProvider(TCRecipeProvider.Runner::new);
        event.createProvider(AuraModifierProvider::new);

        event.createBlockAndItemTags(TCBlockTagsProvider::new, TCItemTagsProvider::new);
        event.createProvider(TCDamageTypeTagsProvider::new);

        event.createProvider((output, lookupProvider) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        TCBlockLootSubProvider::new,
                        LootContextParamSets.BLOCK
                )),
                lookupProvider
        ));
    }
}
