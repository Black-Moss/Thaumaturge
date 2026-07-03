package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCEntityEvents {
    private TCEntityEvents() {}

    @SubscribeEvent
    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(TCEntities.WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WispEntity::checkWispSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.BRAINY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.GIANT_BRAINY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(TCEntities.WISP.get(), WispEntity.createAttributes().build());
        event.put(TCEntities.BRAINY_ZOMBIE.get(), EntityBrainyZombie.createAttributes().build());
        event.put(TCEntities.FIRE_BAT.get(), EntityFireBat.createAttributes().build());
        event.put(TCEntities.MIND_SPIDER.get(), EntityMindSpider.createAttributes().build());
        event.put(TCEntities.GIANT_BRAINY_ZOMBIE.get(), EntityGiantBrainyZombie.createAttributes().build());
        event.put(TCEntities.THAUMIC_SLIME.get(), ThaumicSlime.createAttributes().build());
        event.put(TCEntities.TAINT_CRAWLER.get(), EntityTaintCrawler.createAttributes().build());
        event.put(TCEntities.TAINT_SEED.get(), EntityTaintSeed.createAttributes().build());
        event.put(TCEntities.TAINT_SEED_PRIME.get(), EntityTaintSeedPrime.createAttributes().build());
        event.put(TCEntities.TAINT_SWARM.get(), EntityTaintSwarm.createAttributes().build());
        event.put(TCEntities.TAINTACLE.get(), EntityTaintacle.createAttributes().build());
        event.put(TCEntities.TAINTACLE_SMALL.get(), EntityTaintacleSmall.createAttributes().build());
    }
}
