package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardCalibrate extends TheorycraftCard {
    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.ARTIFICE;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.calibrate.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.calibrate.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.ARTIFICE, 15);
        data.addBonusDraws(1);
        return true;
    }
}
