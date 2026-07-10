package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CardSculpting extends TheorycraftCard {
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
        return Component.translatable("card.thaumcraft.sculpting.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.sculpting.text");
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        return List.of(new CardItemRequirement(new ItemStack(Items.CLAY_BALL), true));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.GOLEMANCY, 20);
        data.addBonusDraws(1);
        return true;
    }
}
