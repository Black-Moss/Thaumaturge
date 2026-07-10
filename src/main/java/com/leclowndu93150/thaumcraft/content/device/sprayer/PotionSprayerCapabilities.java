package com.leclowndu93150.thaumcraft.content.device.sprayer;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class PotionSprayerCapabilities {
    private PotionSprayerCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TCBlockEntities.POTION_SPRAYER.get(),
                (be, side) -> be
        );
    }
}
