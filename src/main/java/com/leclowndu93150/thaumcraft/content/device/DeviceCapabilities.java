package com.leclowndu93150.thaumcraft.content.device;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

@EventBusSubscriber(modid = TCIds.MODID)
public final class DeviceCapabilities {
    private DeviceCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TCBlockEntities.LAMP_GROWTH.get(),
                (be, side) -> be
        );
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TCBlockEntities.LAMP_FERTILITY.get(),
                (be, side) -> be
        );
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TCBlockEntities.CENTRIFUGE.get(),
                (be, side) -> be
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                TCBlockEntities.HUNGRY_CHEST.get(),
                (be, side) -> VanillaContainerWrapper.of(be)
        );
    }
}
