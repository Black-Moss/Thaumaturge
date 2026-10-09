package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;

public final class EntityTaintSpore extends AbstractTaintSpore {
    private static final double MAX_HEALTH = 10.0;
    private static final int SATELLITE_MIN_SIZE = 8;
    private static final float SATELLITE_MIN_SATURATION = 0.85F;
    private static final int SATELLITE_ONE_IN = 4;
    private static final int MAX_BROOD = 6;
    private static final double BROOD_SPREAD = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;

    public EntityTaintSpore(EntityType<? extends EntityTaintSpore> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createSporeAttributes(MAX_HEALTH);
    }

    @Override
    protected boolean requiresStalkSupport() {
        return true;
    }

    @Override
    protected void onBurst(ServerLevel level) {
        RandomSource random = level.getRandom();
        BlockPos pos = blockPosition();
        if (getSporeSize() >= SATELLITE_MIN_SIZE && TaintEcology.getSaturation(level, pos) >= SATELLITE_MIN_SATURATION && random.nextInt(SATELLITE_ONE_IN) == 0) {
            TaintHelper.trySpawnSatelliteSeed(level, pos.below(), random);
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        int brood = Math.min(MAX_BROOD, random.nextInt(getSporeSize() + 1));
        for (int index = 0; index < brood; index++) {
            spawnSpider(level, random);
        }
    }

    private void spawnSpider(ServerLevel level, RandomSource random) {
        Spider spider = EntityType.SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (spider == null) {
            return;
        }
        double x = getX() + (random.nextDouble() - BROOD_SPREAD);
        double z = getZ() + (random.nextDouble() - BROOD_SPREAD);
        spider.snapTo(x, getY(), z, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        MobTraits.add(spider, TTMobTraits.TAINT_BROOD);
        level.addFreshEntity(spider);
    }
}
