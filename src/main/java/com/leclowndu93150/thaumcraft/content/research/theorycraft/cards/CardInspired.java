package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.CategoryComponents;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CardInspired extends TheorycraftCard {
    private @Nullable ResourceKey<IResearchCategory> category;
    private int amount;

    @Override
    public int inspirationCost() {
        return 2;
    }

    @Override
    public @Nullable ResourceKey<IResearchCategory> category() {
        return category;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.inspired.name");
    }

    @Override
    public Component description() {
        return Component.translatable(
                "card.thaumcraft.inspired.text",
                amount,
                category == null ? Component.empty() : CategoryComponents.emphasised(category));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        if (data.categoriesWithTotal().isEmpty()) return false;
        int highest = 0;
        ResourceKey<IResearchCategory> highestKey = null;
        for (ResourceKey<IResearchCategory> key : data.categoriesWithTotal()) {
            int value = data.categoryTotal(key);
            if (value > highest) {
                highest = value;
                highestKey = key;
            }
        }
        if (highestKey == null) return false;
        category = highestKey;
        amount = 10 + highest / 2;
        return true;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (category == null) return false;
        data.addCategoryTotal(category, amount);
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        if (category != null) {
            output.store("category", ResourceKey.codec(IResearchCategory.REGISTRY_KEY), category);
        }
        output.putInt("amount", amount);
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        category = input.read("category", ResourceKey.codec(IResearchCategory.REGISTRY_KEY)).orElse(null);
        amount = input.getIntOr("amount", 0);
    }
}
