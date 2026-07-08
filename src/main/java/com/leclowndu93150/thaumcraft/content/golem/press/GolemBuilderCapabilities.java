package com.leclowndu93150.thaumcraft.content.golem.press;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class GolemBuilderCapabilities {
    private GolemBuilderCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TCBlockEntities.GOLEM_BUILDER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                TCBlockEntities.GOLEM_BUILDER.get(),
                (be, side) -> be.output()
        );
    }
}
