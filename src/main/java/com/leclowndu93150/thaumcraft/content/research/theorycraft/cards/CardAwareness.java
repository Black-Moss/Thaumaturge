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

public final class CardAwareness extends TheorycraftCard {
    private static final float ELDRITCH_INSIGHT_CHANCE = 0.33F;

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
        return Component.translatable("card.thaumcraft.awareness.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.awareness.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.AUROMANCY, 20);
        if (player.getRandom().nextFloat() < ELDRITCH_INSIGHT_CHANCE) {
            data.addCategoryTotal(TCResearchCategories.ELDRITCH, Mth.nextInt(player.getRandom(), 1, 5));
            WarpHelper.addWarp(player, 1, WarpType.NORMAL);
        }
        return true;
    }
}
