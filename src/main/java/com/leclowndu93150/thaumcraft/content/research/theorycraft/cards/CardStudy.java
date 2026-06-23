package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.research.CategoryComponents;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.List;
import java.util.Random;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CardStudy extends TheorycraftCard {
    private @Nullable ResourceKey<IResearchCategory> category;

    @Override
    public boolean isAidOnly() {
        return true;
    }

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public @Nullable ResourceKey<IResearchCategory> category() {
        return category;
    }

    @Override
    public Component name() {
        return category == null
                ? Component.translatable("card.thaumcraft.study.name", Component.empty())
                : Component.translatable("card.thaumcraft.study.name", CategoryComponents.emphasised(category));
    }

    @Override
    public Component description() {
        return category == null
                ? Component.translatable("card.thaumcraft.study.text", Component.empty())
                : Component.translatable("card.thaumcraft.study.text", CategoryComponents.emphasised(category));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        Random rng = new Random(seed());
        List<ResourceKey<IResearchCategory>> available = data.availableCategories(player);
        if (available.isEmpty()) return false;
        category = available.get(rng.nextInt(available.size()));
        return category != null;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (category == null) return false;
        data.addCategoryTotal(category, 15 + player.getRandom().nextInt(11));
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        if (category != null) {
            output.store("category", ResourceKey.codec(IResearchCategory.REGISTRY_KEY), category);
        }
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        category = input.read("category", ResourceKey.codec(IResearchCategory.REGISTRY_KEY)).orElse(null);
    }

}
