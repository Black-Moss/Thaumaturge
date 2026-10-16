package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

final class MatrixPullEffects {
    private static final double SPAWN_LIFT = 1.0;
    private static final double TARGET_XZ_OFFSET = 0.5;
    private static final double TARGET_Y_OFFSET = -0.5;

    private static final int CLIENT_SOURCE_BASE_COUNT = 4;
    private static final int CLIENT_SPARKLE_ODDS = 3;
    private static final float SPARKLE_VIOLET_RED = 0.6F;
    private static final float SPARKLE_PURPLE_RED = 0.4F;
    private static final float SPARKLE_GREEN = 0.2F;
    private static final float SPARKLE_BLUE_MIN = 0.6F;
    private static final float SPARKLE_BLUE_SPAN = 0.3F;
    private static final double CRUMB_BOX_OFFSET_XZ = 0.4;
    private static final double CRUMB_BOX_OFFSET_Y = 1.23;
    private static final double CRUMB_BOX_SPAN = 0.2;
    private static final double CRUMB_ITEM_KICK = 0.03;

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

    private static void spawn(Level level, BlockPos matrix, BlockPos source, ItemStack item) {
        RandomSource random = level.getRandom();
        Vec3 goal = new Vec3(matrix.getX() + TARGET_XZ_OFFSET, matrix.getY() + TARGET_Y_OFFSET, matrix.getZ() + TARGET_XZ_OFFSET);
        if (random.nextInt(CLIENT_SPARKLE_ODDS) == 0) {
            spawnSparkle(level, source, goal, random);
            return;
        }
        ItemStackTemplate template = ItemStackTemplate.fromNonEmptyStack(item);
        boolean block = item.getItem() instanceof BlockItem;
        for (int i = 0; i < CLIENT_SOURCE_BASE_COUNT; i++) {
            if (block) {
                Vec3 start = randomAbove(source, random);
                level.addParticle(new InfusionCrumbsParticleOptions(template, goal.x, goal.y, goal.z, 0.0, 0.0, 0.0), start.x, start.y, start.z, 0.0, 0.0, 0.0);
            } else {
                double x = source.getX() + CRUMB_BOX_OFFSET_XZ + CRUMB_BOX_SPAN * random.nextDouble();
                double y = source.getY() + CRUMB_BOX_OFFSET_Y + CRUMB_BOX_SPAN * random.nextDouble();
                double z = source.getZ() + CRUMB_BOX_OFFSET_XZ + CRUMB_BOX_SPAN * random.nextDouble();
                InfusionCrumbsParticleOptions options = new InfusionCrumbsParticleOptions(template, goal.x, goal.y, goal.z, random.nextGaussian() * CRUMB_ITEM_KICK,
                        random.nextGaussian() * CRUMB_ITEM_KICK, random.nextGaussian() * CRUMB_ITEM_KICK);
                level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    private static void spawnSparkle(Level level, BlockPos source, Vec3 goal, RandomSource random) {
        Vec3 start = randomAbove(source, random);
        float red = Mth.lerp(random.nextFloat(), SPARKLE_PURPLE_RED, SPARKLE_VIOLET_RED);
        float blue = SPARKLE_BLUE_MIN + random.nextFloat() * SPARKLE_BLUE_SPAN;
        level.addParticle(new BoreSparkleParticleOptions(goal.x, goal.y, goal.z, red, SPARKLE_GREEN, blue), start.x, start.y, start.z, 0.0, 0.0, 0.0);
    }

    private static Vec3 randomAbove(BlockPos source, RandomSource random) {
        return new Vec3(source.getX() + random.nextDouble(), source.getY() + SPAWN_LIFT + random.nextDouble(), source.getZ() + random.nextDouble());
    }
}
