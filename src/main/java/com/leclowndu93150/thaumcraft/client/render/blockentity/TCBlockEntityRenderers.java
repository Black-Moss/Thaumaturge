package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCBlockEntityRenderers {
    private TCBlockEntityRenderers() {}

    @SubscribeEvent
    public static void onRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_VOID.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
    }
}
