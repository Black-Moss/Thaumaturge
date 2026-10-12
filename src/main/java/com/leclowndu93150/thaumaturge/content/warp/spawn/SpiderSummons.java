package com.leclowndu93150.thaumaturge.content.warp.spawn;

import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;

public final class SpiderSummons {
    private static final int HORIZONTAL_BOUND = 15;
    private static final int VERTICAL_BOUND = 5;

    private SpiderSummons() {}

    public static boolean summon(ServerLevel level, ServerPlayer player, RandomSource random, boolean illusory) {
        EntityMindSpider spider = TTEntities.MIND_SPIDER.get().create(level, EntitySpawnReason.EVENT);
        if (spider == null) {
            return false;
        }
        for (int attempt = 0; attempt < SpawnSpots.PLACEMENT_ATTEMPTS; attempt++) {
            double x = player.getX() + (random.nextInt(HORIZONTAL_BOUND) - random.nextInt(HORIZONTAL_BOUND));
            double y = player.getY() + (random.nextInt(VERTICAL_BOUND) - random.nextInt(VERTICAL_BOUND));
            double z = player.getZ() + (random.nextInt(HORIZONTAL_BOUND) - random.nextInt(HORIZONTAL_BOUND));
            float yaw = random.nextFloat() * SpawnSpots.FULL_TURN_DEGREES;
            if (!SpawnSpots.standsOnFullBlock(level, BlockPos.containing(x, y, z))) {
                continue;
            }
            spider.snapTo(x, y, z, yaw, 0.0F);
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
