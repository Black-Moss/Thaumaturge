package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardSynergy extends TheorycraftCard {
    private static final int DRAIN_TOTAL = 15;
    private static final int MAX_DRAIN_TRIES = 1000;
    private static final List<ResourceKey<IResearchCategory>> SOURCES = List.of(
            TCResearchCategories.ARTIFICE, TCResearchCategories.ALCHEMY, TCResearchCategories.INFUSION);

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.GOLEMANCY;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.synergy.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.synergy.text");
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        return sourceTotal(data) >= DRAIN_TOTAL;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (sourceTotal(data) < DRAIN_TOTAL) return false;
        int remaining = DRAIN_TOTAL;
        int tries = 0;
        while (remaining > 0 && tries < MAX_DRAIN_TRIES) {
            tries++;
            for (ResourceKey<IResearchCategory> source : SOURCES) {
                if (data.categoryTotal(source) > 0) {
                    data.addCategoryTotal(source, -1);
                    if (--remaining <= 0) break;
                }
            }
        }
        data.addCategoryTotal(TCResearchCategories.GOLEMANCY, 30);
        data.incrementPenalty();
        return true;
    }

    private static int sourceTotal(IResearchTableData data) {
        int total = 0;
        for (ResourceKey<IResearchCategory> source : SOURCES) {
            total += data.categoryTotal(source);
        }
        return total;
    }
}
