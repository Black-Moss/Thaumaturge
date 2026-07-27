package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ResearchTableCapabilities {
    private ResearchTableCapabilities() {}

    @SubscribeEvent
    public static void onRegister(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                TCBlockEntities.RESEARCH_TABLE.get(),
                (blockEntity, side) -> blockEntity.items()
        );
    }
}
