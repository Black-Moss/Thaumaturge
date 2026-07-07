package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCBlockEntityRenderers {
    private static final float RECHARGE_PEDESTAL_ITEM_SCALE = 1.5F;

    private TCBlockEntityRenderers() {}

    @SubscribeEvent
    public static void onRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.INFUSION_MATRIX.get(), InfusionMatrixRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FOCAL_MANIPULATOR.get(), FocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.RECHARGE_PEDESTAL.get(),
                context -> new PedestalRenderer<>(context, RECHARGE_PEDESTAL_ITEM_SCALE));
        event.registerBlockEntityRenderer(TCBlockEntities.JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_VOID.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.BANNER.get(), BannerRenderer::new);
    }
}
