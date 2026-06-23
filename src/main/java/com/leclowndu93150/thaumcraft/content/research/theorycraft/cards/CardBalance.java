package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardBalance extends TheorycraftCard {
    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.balance.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.balance.text");
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        return canBalance(data);
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (!canBalance(data)) return false;
        Set<ResourceKey<IResearchCategory>> categories = data.categoriesWithTotal();
        int unblockedCount = 0;
        int unblockedTotal = 0;
        for (ResourceKey<IResearchCategory> key : categories) {
            if (data.isCategoryBlocked(key)) continue;
            unblockedTotal += data.categoryTotal(key);
            unblockedCount++;
        }
        if (unblockedCount == 0) return false;
        int even = unblockedTotal / unblockedCount;
        for (ResourceKey<IResearchCategory> key : categories) {
            if (!data.isCategoryBlocked(key)) {
                data.setCategoryTotal(key, even);
            }
        }
        data.addCategoryTotal(TCResearchCategories.BASICS, 5);
        data.incrementPenalty();
        return true;
    }

    private static boolean canBalance(IResearchTableData data) {
        Set<ResourceKey<IResearchCategory>> categories = data.categoriesWithTotal();
        int unblockedCount = 0;
        int unblockedTotal = 0;
        int blocked = 0;
        for (ResourceKey<IResearchCategory> key : categories) {
            if (data.isCategoryBlocked(key)) {
                blocked++;
            } else {
                unblockedTotal += data.categoryTotal(key);
                unblockedCount++;
            }
        }
        return blocked < categories.size() - 1 && unblockedTotal >= unblockedCount;
    }
}
