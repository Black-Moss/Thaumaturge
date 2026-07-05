package com.leclowndu93150.thaumcraft.client.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.model.entity.FireBatModel;
import com.leclowndu93150.thaumcraft.client.model.entity.MatrixCubeModel;
import com.leclowndu93150.thaumcraft.client.model.entity.TCBannerModel;
import com.leclowndu93150.thaumcraft.client.model.entity.TaintSeedModel;
import com.leclowndu93150.thaumcraft.client.model.entity.TaintacleModel;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCEntityRenderers {
    private static final float TAINTACLE_SHADOW = 0.6F;
    private static final float TAINTACLE_SMALL_SHADOW = 0.2F;
    private static final float TAINT_SEED_SHADOW = 0.4F;
    private static final float TAINT_SEED_PRIME_SHADOW = 0.6F;

    private TCEntityRenderers() {}

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TCModelLayers.TAINTACLE,
                () -> TaintacleModel.createLayer(TaintacleModel.TAINTACLE_LENGTH));
        event.registerLayerDefinition(TCModelLayers.TAINTACLE_SMALL,
                () -> TaintacleModel.createLayer(TaintacleModel.TAINTACLE_SMALL_LENGTH));
        event.registerLayerDefinition(TCModelLayers.TAINT_SEED, TaintSeedModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.FIRE_BAT, FireBatModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.TC_BANNER, TCBannerModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.MATRIX_CUBE, MatrixCubeModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TCEntities.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(TCEntities.FLUX_RIFT.get(), FluxRiftRenderer::new);
        event.registerEntityRenderer(TCEntities.CAUSALITY_COLLAPSER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(TCEntities.BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(TCEntities.GIANT_BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(TCEntities.FIRE_BAT.get(), FireBatRenderer::new);
        event.registerEntityRenderer(TCEntities.MIND_SPIDER.get(), MindSpiderRenderer::new);
        event.registerEntityRenderer(TCEntities.THAUMIC_SLIME.get(), ThaumicSlimeRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_CRAWLER.get(), TaintCrawlerRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_SEED.get(),
                context -> new TaintSeedRenderer(context, TAINT_SEED_SHADOW));
        event.registerEntityRenderer(TCEntities.TAINT_SEED_PRIME.get(),
                context -> new TaintSeedRenderer(context, TAINT_SEED_PRIME_SHADOW));
        event.registerEntityRenderer(TCEntities.TAINT_SWARM.get(), TaintSwarmRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINTACLE.get(),
                context -> new TaintacleRenderer(context, TCModelLayers.TAINTACLE,
                        TaintacleModel.TAINTACLE_LENGTH, TAINTACLE_SHADOW));
        event.registerEntityRenderer(TCEntities.TAINTACLE_SMALL.get(),
                context -> new TaintacleRenderer(context, TCModelLayers.TAINTACLE_SMALL,
                        TaintacleModel.TAINTACLE_SMALL_LENGTH, TAINTACLE_SMALL_SHADOW));
        event.registerEntityRenderer(TCEntities.FALLING_TAINT.get(), FallingTaintRenderer::new);
        event.registerEntityRenderer(TCEntities.BOTTLE_TAINT.get(), BottleTaintRenderer::new);
        event.registerEntityRenderer(TCEntities.SPECIAL_ITEM.get(), SpecialItemRenderer::new);
    }
}
