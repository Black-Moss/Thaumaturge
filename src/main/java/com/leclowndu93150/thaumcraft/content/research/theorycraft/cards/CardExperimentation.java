package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class CardExperimentation extends TheorycraftCard {
    @Override
    public int inspirationCost() {
        return 2;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.experimentation.name");
    }

    @Override
    public Component description() {
        return Component.translatable("card.thaumcraft.experimentation.text");
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        List<ResourceKey<IResearchCategory>> all = data.availableCategories(player);
        if (all.isEmpty()) return false;
        ResourceKey<IResearchCategory> pick = all.get(player.getRandom().nextInt(all.size()));
        data.addCategoryTotal(pick, 15 + player.getRandom().nextInt(16));
        data.addCategoryTotal(TCResearchCategories.BASICS, 1 + player.getRandom().nextInt(10));
        return true;
    }
}
