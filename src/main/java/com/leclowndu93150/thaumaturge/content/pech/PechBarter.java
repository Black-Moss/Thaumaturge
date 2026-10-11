package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class PechBarter {
    private static final int PERCENT = 100;
    private static final int SOUR_BASE_PERCENT = 1;
    private static final int SOUR_BUDGET_DIVISOR = 2;
    private static final float HAGGLE_UP_CHANCE = 0.2F;
    private static final float HAGGLE_DOWN_CHANCE = 0.4F;
    private static final int HAGGLE_STEPS = 3;
    private static final int MAX_TIER = 5;
    private static final int HOARD_TIER = 1;
    private static final float HOARD_PAYOUT_CHANCE = 0.5F;
    private static final int RISKY_TIER = 4;
    private static final float RISKY_MISS_CHANCE = 0.5F;
    private static final int HOARD_TAKE = 1;

    private PechBarter() {}

    public static List<ItemStack> haggle(EntityPech pech, ItemStack offered, RandomSource random, HolderLookup.Provider registries) {
        int budget = pech.getValue(offered);
        maybeTurnSour(pech, budget, random);
        budget += swing(random);
        List<PechTrades.Entry> trades = PechTrades.tradesFor(pech.variant(), registries);
        List<ItemStack> payout = new ArrayList<>();
        while (budget > 0) {
            int tier = roundSize(budget, random);
            budget -= tier;
            ItemStack paid = payRound(pech, tier, trades, random);
            if (!paid.isEmpty()) {
                payout.add(paid);
            }
        }
        return payout;
    }

    private static void maybeTurnSour(EntityPech pech, int budget, RandomSource random) {
        int sourPercent = budget / SOUR_BUDGET_DIVISOR + SOUR_BASE_PERCENT;
        if (random.nextInt(PERCENT) >= sourPercent) {
            return;
        }
        pech.setDomesticated(false);
        pech.level().playSound(null, pech.getX(), pech.getY(), pech.getZ(), TTSounds.PECH_TRADE.get(), pech.getSoundSource(), pech.getSoundVolume(), pech.getVoicePitch());
    }

    private static int swing(RandomSource random) {
        float mood = random.nextFloat();
        if (mood < HAGGLE_UP_CHANCE) {
            return random.nextInt(HAGGLE_STEPS);
        }
        if (mood < HAGGLE_UP_CHANCE + HAGGLE_DOWN_CHANCE) {
            return -random.nextInt(HAGGLE_STEPS);
        }
        return 0;
    }

    private static int roundSize(int budget, RandomSource random) {
        int halfUp = (budget + 1) / 2;
        int gamble = 1 + random.nextInt(budget);
        return Math.min(MAX_TIER, Math.max(halfUp, gamble));
    }

    private static ItemStack payRound(EntityPech pech, int tier, List<PechTrades.Entry> trades, RandomSource random) {
        if (tier == HOARD_TIER && random.nextFloat() < HOARD_PAYOUT_CHANCE) {
            ItemStack fromHoard = takeFromHoard(pech.loot, random);
            if (!fromHoard.isEmpty()) {
                return fromHoard;
            }
        }
        if (tier >= RISKY_TIER && random.nextFloat() < RISKY_MISS_CHANCE) {
            return ItemStack.EMPTY;
        }
        return pickOfTier(trades, tier, random);
    }

    private static ItemStack takeFromHoard(List<ItemStack> hoard, RandomSource random) {
        List<ItemStack> filled = hoard.stream().filter(stack -> !stack.isEmpty()).toList();
        if (filled.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return filled.get(random.nextInt(filled.size())).split(HOARD_TAKE);
    }

    private static ItemStack pickOfTier(List<PechTrades.Entry> trades, int tier, RandomSource random) {
        List<PechTrades.Entry> matching = trades.stream().filter(entry -> entry.tier() == tier).toList();
        if (matching.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return matching.get(random.nextInt(matching.size())).stack().copy();
    }
}
