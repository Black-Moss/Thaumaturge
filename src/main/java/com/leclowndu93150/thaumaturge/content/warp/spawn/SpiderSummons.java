package com.leclowndu93150.thaumaturge.content.warp.spawn;

import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;

public final class SpiderSummons {
    private SpiderSummons() {}

    public static boolean summon(ServerLevel level, ServerPlayer player, RandomSource random, boolean illusory) {
        EntityMindSpider spider = TTEntities.MIND_SPIDER.get().create(level, EntitySpawnReason.EVENT);
        if (spider == null) {
            return false;
        }
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < SpawnSpots.PLACEMENT_ATTEMPTS; attempt++) {
            BlockPos cell = SpawnSpots.ringCell(origin, random);
            if (!SpawnSpots.standsOnFullBlock(level, cell)) {
                continue;
            }
            spider.snapTo(cell.getX() + SpawnSpots.CELL_CENTER, cell.getY(), cell.getZ() + SpawnSpots.CELL_CENTER, random.nextFloat() * SpawnSpots.FULL_TURN_DEGREES, 0.0F);
            if (!SpawnSpots.hasFreeSpace(level, spider)) {
                continue;
            }
            if (illusory) {
                spider.bindIllusion(player.getGameProfile().name());
            }
            if (level.addFreshEntity(spider)) {
                spider.setTarget(player);
            }
            return true;
        }
        return true;
    }
}
