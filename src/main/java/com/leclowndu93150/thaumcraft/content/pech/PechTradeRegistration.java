package com.leclowndu93150.thaumcraft.content.pech;

import com.leclowndu93150.thaumcraft.TCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class PechTradeRegistration {
    private PechTradeRegistration() {}

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PechTradeTable.REGISTRY_KEY, PechTradeTable.CODEC);
    }
}
