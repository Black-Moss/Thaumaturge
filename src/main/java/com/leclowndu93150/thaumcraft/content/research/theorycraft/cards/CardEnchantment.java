package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class CardEnchantment extends TheorycraftCard {
    private static final int LEVEL_COST = 5;

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
        return Component.translatable("card.thaumcraft.enchantment.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.enchantment.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (player.experienceLevel < LEVEL_COST) return false;
        player.giveExperienceLevels(-LEVEL_COST);
        data.addCategoryTotal(TCResearchCategories.INFUSION, Mth.nextInt(player.getRandom(), 15, 20));
        data.addCategoryTotal(TCResearchCategories.AUROMANCY, Mth.nextInt(player.getRandom(), 15, 20));
        return true;
    }
}
