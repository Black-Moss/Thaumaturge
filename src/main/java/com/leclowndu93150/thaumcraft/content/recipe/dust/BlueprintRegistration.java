package com.leclowndu93150.thaumcraft.content.recipe.dust;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.recipe.Blueprint;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class BlueprintRegistration {
    private BlueprintRegistration() {}

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Blueprint.REGISTRY_KEY, Blueprint.CODEC, Blueprint.CODEC);
    }
}
