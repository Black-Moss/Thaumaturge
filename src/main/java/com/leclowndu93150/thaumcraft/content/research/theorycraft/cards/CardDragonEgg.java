package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class CardDragonEgg extends TheorycraftCard {
    private static final int ROLLS = 10;

    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.dragonegg.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.dragonegg.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        for (int roll = 0; roll < ROLLS; roll++) {
            data.addCategoryTotal(CardHelper.randomCategory(player), Mth.nextInt(player.getRandom(), 2, 5));
        }
        return true;
    }
}
