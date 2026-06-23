package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class AspectRegistration {
    private AspectRegistration() {}

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(IAspect.REGISTRY_KEY, Aspect.CODEC, Aspect.CODEC);
    }
}
