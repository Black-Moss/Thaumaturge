package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardRethink extends TheorycraftCard {
    @Override
    public int inspirationCost() {
        return -1;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.rethink.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.rethink.text");
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        return data.totalUnblockedPoints() >= 10;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (data.totalUnblockedPoints() < 10) return false;
        int toRemove = Math.min(data.totalUnblockedPoints(), 10);
        int safety = 0;
        Set<ResourceKey<IResearchCategory>> categories = data.categoriesWithTotal();
        while (toRemove > 0 && safety < 1000) {
            safety++;
            boolean changed = false;
            for (ResourceKey<IResearchCategory> key : categories) {
                if (data.categoryTotal(key) <= 0) continue;
                data.addCategoryTotal(key, -1);
                changed = true;
                if (--toRemove <= 0) break;
            }
            if (!changed) break;
        }
        data.addBonusDraws(1);
        data.addCategoryTotal(TCResearchCategories.BASICS, 1 + player.getRandom().nextInt(10));
        return true;
    }
}
