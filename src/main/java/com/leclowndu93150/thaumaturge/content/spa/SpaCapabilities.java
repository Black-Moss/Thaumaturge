package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class SpaCapabilities {
    private SpaCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, TCBlockEntities.SPA.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                TCBlockEntities.SPA.get(),
                (be, side) -> side == Direction.UP ? null : be.getItems());
    }
}
