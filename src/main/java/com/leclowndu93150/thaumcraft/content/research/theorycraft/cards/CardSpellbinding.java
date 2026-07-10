package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardSpellbinding extends TheorycraftCard {
    private static final int MAX_LEVELS = 5;
    private static final int POINTS_PER_LEVEL = 5;

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.AUROMANCY;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.spellbinding.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.spellbinding.text");
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        return player.experienceLevel > 0;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (player.experienceLevel <= 0) return false;
        int levels = Math.min(MAX_LEVELS, player.experienceLevel);
        data.addCategoryTotal(TCResearchCategories.AUROMANCY, levels * POINTS_PER_LEVEL);
        player.giveExperienceLevels(-levels);
        return true;
    }
}
