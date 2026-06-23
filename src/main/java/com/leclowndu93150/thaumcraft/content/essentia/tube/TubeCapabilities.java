package com.leclowndu93150.thaumcraft.content.essentia.tube;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TubeCapabilities {
    private TubeCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_VALVE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_RESTRICT.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_ONEWAY.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TCBlockEntities.TUBE_BUFFER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.ASPECT_QUERY, TCBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
    }
}
