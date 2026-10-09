package com.leclowndu93150.thaumaturge.content.golem;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

final class GolemBillDrops {
    private static final float BASE_CHANCE = 0.3F;
    private static final float LOOTING_BONUS = 0.15F;
    private static final int MINIMUM_SURVIVORS = 1;

    private GolemBillDrops() {}

    static float chance(int looting) {
        return BASE_CHANCE + LOOTING_BONUS * looting;
    }

    static ItemStack roll(RandomSource random, ItemStack line, float chance) {
        if (random.nextFloat() >= chance) {
            return ItemStack.EMPTY;
        }
        return line.copyWithCount(survivors(random, line.getCount()));
    }

    private static int survivors(RandomSource random, int count) {
        return count <= MINIMUM_SURVIVORS ? count : MINIMUM_SURVIVORS + random.nextInt(count);
    }
}
