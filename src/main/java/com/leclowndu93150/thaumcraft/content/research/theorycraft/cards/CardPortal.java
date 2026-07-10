package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
import com.leclowndu93150.thaumcraft.api.warp.WarpType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class CardPortal extends TheorycraftCard {
    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int inspirationCost() {
        return -1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.ELDRITCH;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.portal.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.portal.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(CardHelper.randomCategory(player), Mth.nextInt(player.getRandom(), 5, 10));
        data.addCategoryTotal(CardHelper.randomCategory(player), Mth.nextInt(player.getRandom(), 5, 10));
        data.addCategoryTotal(TCResearchCategories.ELDRITCH, Mth.nextInt(player.getRandom(), 5, 10));
        data.addBonusDraws(2);
        WarpHelper.addWarp(player, 5, WarpType.TEMPORARY);
        WarpHelper.addWarp(player, 1, WarpType.NORMAL);
        return true;
    }
}
