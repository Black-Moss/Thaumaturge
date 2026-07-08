package com.leclowndu93150.thaumcraft.client.screen;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.screen.casters.FocalManipulatorScreen;
import com.leclowndu93150.thaumcraft.client.screen.golem.GolemBuilderScreen;
import com.leclowndu93150.thaumcraft.client.screen.golem.SealScreen;
import com.leclowndu93150.thaumcraft.client.screen.pip.BlockPreviewRenderState;
import com.leclowndu93150.thaumcraft.client.screen.pip.BlockPreviewRenderer;
import com.leclowndu93150.thaumcraft.client.screen.research.ResearchTableScreen;
import com.leclowndu93150.thaumcraft.client.screen.workbench.ArcaneWorkbenchScreen;
import com.leclowndu93150.thaumcraft.registry.TCMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

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
        event.register(TCMenus.GOLEM_BUILDER.get(), GolemBuilderScreen::new);
        event.register(TCMenus.SEAL.get(), SealScreen::new);
        event.register(TCMenus.VOID_SIPHON.get(), VoidSiphonScreen::new);
        event.register(TCMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
        event.register(TCMenus.PECH.get(), PechScreen::new);
    }

    @SubscribeEvent
    public static void registerPIP(RegisterPictureInPictureRenderersEvent event){
        event.register(BlockPreviewRenderState.class, BlockPreviewRenderer::new);
    }
}
