package com.leclowndu93150.thaumcraft.content.research;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanEntry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ResearchRegistration {
    private ResearchRegistration() {}

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(IResearchCategory.REGISTRY_KEY, ResearchCategory.CODEC, ResearchCategory.CODEC);
        event.dataPackRegistry(IResearchEntry.REGISTRY_KEY, ResearchEntry.CODEC, ResearchEntry.CODEC);
        event.dataPackRegistry(ScanEntry.REGISTRY_KEY, ScanEntry.CODEC, ScanEntry.CODEC);
    }
}
