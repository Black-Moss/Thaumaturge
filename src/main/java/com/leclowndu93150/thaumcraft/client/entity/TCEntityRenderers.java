package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCEntityRenderers {
    private TCEntityRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TCEntities.THAUMIC_SLIME.get(), ThaumicSlimeRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_CRAWLER.get(), TaintCrawlerRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_SEED.get(), TaintSeedRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_SEED_PRIME.get(), TaintSeedRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_SWARM.get(), TaintSwarmRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINTACLE.get(), TaintacleRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINTACLE_SMALL.get(), TaintacleRenderer::new);
        event.registerEntityRenderer(TCEntities.FALLING_TAINT.get(), FallingTaintRenderer::new);
        event.registerEntityRenderer(TCEntities.BOTTLE_TAINT.get(), BottleTaintRenderer::new);
    }
}
