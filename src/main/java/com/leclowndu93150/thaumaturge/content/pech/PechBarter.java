package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class PechBarter {
    private static final int PERCENT = 100;
    private static final int SOUR_BUDGET_DIVISOR = 2;
    private static final int SOUR_BASE_PERCENT = 1;
    private static final float SOUR_VOLUME = 0.4F;
    private static final float SOUR_PITCH = 1.0F;
    private static final int SWING_RANGE = 3;
    private static final int SWING_RAISE_BELOW = 20;
    private static final int SWING_LOWER_BELOW = 60;
    private static final int MIN_TIER = 1;
    private static final int MAX_TIER = 5;
    private static final int LOOT_ONLY_MAX_TIER = 1;
    private static final int GUARANTEED_MAX_TIER = 3;

    private static final Predicate<ItemStack> NON_EMPTY = Predicate.not(ItemStack::isEmpty);

    private PechBarter() {}

    public static List<ItemStack> haggle(EntityPech pech, ItemStack offered, RandomSource random, HolderLookup.Provider registries) {
        int budget = pech.getValue(offered);
        if (random.nextInt(PERCENT) < Math.min(PERCENT, budget / SOUR_BUDGET_DIVISOR + SOUR_BASE_PERCENT)) {
            pech.setDomesticated(false);
            pech.playSound(TTSounds.PECH_TRADE.get(), SOUR_VOLUME, SOUR_PITCH);
        }
        int remaining = swing(budget, random);
        List<ItemStack> payout = new ArrayList<>();
        if (remaining > 0) {
            payoutRounds(pech, remaining, PechTrades.tradesFor(pech.variant(), registries), random, payout);
        }
        return payout;
    }

    private static void payoutRounds(EntityPech pech, int budget, List<PechTrades.Entry> table, RandomSource random, List<ItemStack> payout) {
        for (int remaining = budget; remaining > 0;) {
            int floor = (remaining + 1) / 2;
            int tier = Math.min(MAX_TIER, Math.max(floor, Mth.nextInt(random, 1, remaining)));
            remaining -= tier;
            ItemStack reward = roundReward(pech, tier, table, random);
            if (!reward.isEmpty()) {
                payout.add(reward);
            }
        }
    }

    private static int swing(int budget, RandomSource random) {
        int roll = random.nextInt(PERCENT);
        if (roll < SWING_RAISE_BELOW) {
            return budget + random.nextInt(SWING_RANGE);
        }
        if (roll < SWING_LOWER_BELOW) {
            return budget - random.nextInt(SWING_RANGE);
        }
        return budget;
    }

    private static ItemStack roundReward(EntityPech pech, int tier, List<PechTrades.Entry> table, RandomSource random) {
        if (tier == LOOT_ONLY_MAX_TIER && hasLoot(pech.loot) && random.nextBoolean()) {
            return takeLootItem(pech.loot, random);
        }
        if (tier > GUARANTEED_MAX_TIER && !random.nextBoolean()) {
            return ItemStack.EMPTY;
        }
        List<PechTrades.Entry> candidates = table.stream().filter(entry -> entry.tier() == tier).toList();
        if (candidates.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return candidates.get(random.nextInt(candidates.size())).stack().copy();
    }

    private static boolean hasLoot(NonNullList<ItemStack> loot) {
        return loot.stream().anyMatch(NON_EMPTY);
    }

    private static ItemStack takeLootItem(NonNullList<ItemStack> loot, RandomSource random) {
        List<ItemStack> filled = loot.stream().filter(NON_EMPTY).toList();
        ItemStack source = filled.get(random.nextInt(filled.size()));
        ItemStack taken = source.copyWithCount(1);
        source.shrink(1);
        return taken;
    }
}
