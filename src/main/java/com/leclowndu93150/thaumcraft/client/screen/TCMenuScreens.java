package com.leclowndu93150.thaumcraft.client.screen;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.screen.casters.FocalManipulatorScreen;
import com.leclowndu93150.thaumcraft.client.screen.construct.ArcaneBoreScreen;
import com.leclowndu93150.thaumcraft.content.entity.construct.MenuTurretBasic;
import com.leclowndu93150.thaumcraft.client.screen.construct.TurretAdvancedScreen;
import com.leclowndu93150.thaumcraft.client.screen.construct.TurretBasicScreen;
import com.leclowndu93150.thaumcraft.client.screen.casters.FocusPouchScreen;
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
        event.register(TCMenus.FOCUS_POUCH.get(), FocusPouchScreen::new);
        event.register(TCMenus.TURRET_BASIC.get(), TurretBasicScreen<MenuTurretBasic>::new);
        event.register(TCMenus.TURRET_ADVANCED.get(), TurretAdvancedScreen::new);
        event.register(TCMenus.ARCANE_BORE.get(), ArcaneBoreScreen::new);
        event.register(TCMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
    }

    @SubscribeEvent
    public static void registerPIP(RegisterPictureInPictureRenderersEvent event){
        event.register(BlockPreviewRenderState.class, BlockPreviewRenderer::new);
    }
}
