package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class CrucibleCapabilities {
    private CrucibleCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AspectCapabilities.CONTAINER,
                TCBlockEntities.CRUCIBLE.get(),
                (be, side) -> be
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                TCBlockEntities.CRUCIBLE.get(),
                (be, side) -> be.getTank()
        );
    }
}
