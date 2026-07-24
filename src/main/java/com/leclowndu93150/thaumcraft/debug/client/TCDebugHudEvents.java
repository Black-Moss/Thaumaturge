package com.leclowndu93150.thaumcraft.debug.client;

import com.leclowndu93150.thaumcraft.TCIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = Dist.CLIENT, modid = TCIds.MODID)
public final class TCDebugHudEvents {
    private TCDebugHudEvents() {}

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("raycast_debug"), new RaycastDebugOverlay());
    }
}
