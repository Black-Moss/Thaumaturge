package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCEntityEvents {
    private TCEntityEvents() {}

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(TCEntities.THAUMIC_SLIME.get(), ThaumicSlime.createAttributes().build());
        event.put(TCEntities.TAINT_CRAWLER.get(), EntityTaintCrawler.createAttributes().build());
        event.put(TCEntities.TAINT_SEED.get(), EntityTaintSeed.createAttributes().build());
        event.put(TCEntities.TAINT_SEED_PRIME.get(), EntityTaintSeedPrime.createAttributes().build());
        event.put(TCEntities.TAINT_SWARM.get(), EntityTaintSwarm.createAttributes().build());
        event.put(TCEntities.TAINTACLE.get(), EntityTaintacle.createAttributes().build());
        event.put(TCEntities.TAINTACLE_SMALL.get(), EntityTaintacleSmall.createAttributes().build());
    }
}
