package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardPonder extends TheorycraftCard {
    @Override
    public int inspirationCost() {
        return 2;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.ponder.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.ponder.text");
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        return countUnblocked(data) > 0;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        Set<ResourceKey<IResearchCategory>> categories = data.categoriesWithTotal();
        if (categories.isEmpty() || countUnblocked(data) == 0) return false;
        int remaining = 25;
        int safety = 0;
        while (remaining > 0 && safety < 1000) {
            safety++;
            for (ResourceKey<IResearchCategory> key : categories) {
                if (data.isCategoryBlocked(key)) continue;
                data.addCategoryTotal(key, 1);
                if (--remaining <= 0) break;
            }
        }
        data.addCategoryTotal(TCResearchCategories.BASICS, 5);
        data.addBonusDraws(1);
        return remaining < 25;
    }

    private static int countUnblocked(IResearchTableData data) {
        int n = 0;
        for (ResourceKey<IResearchCategory> key : data.categoriesWithTotal()) {
            if (!data.isCategoryBlocked(key)) n++;
        }
        return n;
    }
}
