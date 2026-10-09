package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

final class MatrixPullEffects {
    private static final int CLIENT_SOURCE_BASE_COUNT = 4;
    private static final float CLIENT_SPARKLE_CHANCE = 0.33F;
    private static final float SPARKLE_RED_BASE = 0.4F;
    private static final float SPARKLE_RED_SPREAD = 0.2F;
    private static final float SPARKLE_GREEN = 0.2F;
    private static final float SPARKLE_BLUE_BASE = 0.6F;
    private static final float SPARKLE_BLUE_SPREAD = 0.3F;
    private static final double SPAWN_LIFT = 1.0;
    private static final double CRUMB_XZ_BASE = 0.4;
    private static final double CRUMB_XZ_SPREAD = 0.2;
    private static final double CRUMB_Y_BASE = 1.23;
    private static final double CRUMB_Y_SPREAD = 0.2;
    private static final double CRUMB_SPEED = 0.03;
    private static final double TARGET_XZ_OFFSET = 0.5;
    private static final double TARGET_Y_OFFSET = -0.5;

    private MatrixPullEffects() {}

    static void tickSources(Level level, BlockPos matrix, Map<BlockPos, Integer> sources) {
        Iterator<Map.Entry<BlockPos, Integer>> iterator = sources.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            if (entry.getValue() <= 0) {
                iterator.remove();
                continue;
            }
            BlockEntity target = level.getBlockEntity(entry.getKey());
            if (target instanceof BlockEntityPedestal pedestal) {
                if (!pedestal.getItem().isEmpty()) {
                    spawn(level, matrix, entry.getKey(), pedestal.getItem());
                }
                entry.setValue(entry.getValue() - 1);
            } else {
                entry.setValue(0);
            }
        }
    }

    private static void spawn(Level level, BlockPos matrix, BlockPos pedestal, ItemStack stack) {
        RandomSource random = level.getRandom();
        double targetX = matrix.getX() + TARGET_XZ_OFFSET;
        double targetY = matrix.getY() + TARGET_Y_OFFSET;
        double targetZ = matrix.getZ() + TARGET_XZ_OFFSET;
        if (random.nextFloat() < CLIENT_SPARKLE_CHANCE) {
            BoreSparkleParticleOptions sparkle = new BoreSparkleParticleOptions(targetX, targetY, targetZ, SPARKLE_RED_BASE + SPARKLE_RED_SPREAD * random.nextFloat(), SPARKLE_GREEN,
                    SPARKLE_BLUE_BASE + SPARKLE_BLUE_SPREAD * random.nextFloat());
            level.addParticle(sparkle, pedestal.getX() + random.nextFloat(), pedestal.getY() + SPAWN_LIFT + random.nextFloat(), pedestal.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
            return;
        }
        boolean blockItem = stack.getItem() instanceof BlockItem;
        ItemStackTemplate template = new ItemStackTemplate(stack.getItem());
        for (int i = 0; i < CLIENT_SOURCE_BASE_COUNT; i++) {
            if (blockItem) {
                level.addParticle(new InfusionCrumbsParticleOptions(template, targetX, targetY, targetZ, 0.0, 0.0, 0.0), pedestal.getX() + random.nextFloat(),
                        pedestal.getY() + SPAWN_LIFT + random.nextFloat(), pedestal.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
            } else {
                InfusionCrumbsParticleOptions crumb = new InfusionCrumbsParticleOptions(template, targetX, targetY, targetZ, random.nextGaussian() * CRUMB_SPEED, random.nextGaussian() * CRUMB_SPEED,
                        random.nextGaussian() * CRUMB_SPEED);
                level.addParticle(crumb, pedestal.getX() + CRUMB_XZ_BASE + CRUMB_XZ_SPREAD * random.nextFloat(), pedestal.getY() + CRUMB_Y_BASE + CRUMB_Y_SPREAD * random.nextFloat(),
                        pedestal.getZ() + CRUMB_XZ_BASE + CRUMB_XZ_SPREAD * random.nextFloat(), 0.0, 0.0, 0.0);
            }
        }
    }
}
