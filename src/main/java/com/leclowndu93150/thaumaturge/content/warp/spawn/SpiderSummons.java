package com.leclowndu93150.thaumaturge.content.warp.spawn;

import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;

public final class SpiderSummons {
    private static final int HORIZONTAL_SPREAD = 15;
    private static final int VERTICAL_SPREAD = 5;

    private SpiderSummons() {}

    public static boolean summon(ServerLevel level, ServerPlayer player, RandomSource random, boolean illusory) {
        for (int attempt = 0; attempt < SpawnSpots.PLACEMENT_ATTEMPTS; attempt++) {
            EntityMindSpider spider = TTEntities.MIND_SPIDER.get().create(level, EntitySpawnReason.EVENT);
            if (spider == null) {
                return false;
            }
            double x = player.getX() + SpawnSpots.symmetricSpread(random, HORIZONTAL_SPREAD);
            double y = player.getY() + SpawnSpots.symmetricSpread(random, VERTICAL_SPREAD);
            double z = player.getZ() + SpawnSpots.symmetricSpread(random, HORIZONTAL_SPREAD);
            spider.snapTo(x, y, z, random.nextFloat() * SpawnSpots.FULL_TURN_DEGREES, 0.0F);
            BlockPos cell = spider.blockPosition();
            if (!SpawnSpots.standsOnFullBlock(level, cell) || !SpawnSpots.hasFreeSpace(level, spider)) {
                spider.discard();
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
