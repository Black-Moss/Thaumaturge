package com.leclowndu93150.thaumcraft.content.crucible;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import com.leclowndu93150.thaumcraft.registry.TCItems;
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
