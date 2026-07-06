package com.leclowndu93150.thaumcraft.client.screen;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.screen.casters.FocalManipulatorScreen;
import com.leclowndu93150.thaumcraft.client.screen.research.ResearchTableScreen;
import com.leclowndu93150.thaumcraft.client.screen.workbench.ArcaneWorkbenchScreen;
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
        event.register(TCMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(TCMenus.SMELTER.get(), SmelterScreen::new);
        event.register(TCMenus.SPA.get(), SpaScreen::new);
        event.register(TCMenus.FOCAL_MANIPULATOR.get(), FocalManipulatorScreen::new);
    }
}
