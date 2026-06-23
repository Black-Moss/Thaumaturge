package com.leclowndu93150.thaumcraft.client.screen.research.menu;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.screen.research.ResearchTableScreen;
import com.leclowndu93150.thaumcraft.registry.TCMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCMenuScreens {
    private TCMenuScreens() {}

    @SubscribeEvent
    public static void onRegister(RegisterMenuScreensEvent event) {
        event.register(TCMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
    }
}
