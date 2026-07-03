package com.leclowndu93150.thaumcraft.client.hud;

import com.leclowndu93150.thaumcraft.TCIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = Dist.CLIENT, modid = TCIds.MODID)
public final class TCHudEvents {
    private TCHudEvents() {}

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("aura_hud"), new AuraHudOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("knowledge_gain"), new KnowledgeGainOverlay());
    }
}
