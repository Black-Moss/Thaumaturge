package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CardBeacon extends TheorycraftCard {
    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int inspirationCost() {
        return -2;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.beacon.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.beacon.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addBonusDraws(1);
        data.incrementPenalty();
        return true;
    }
}
