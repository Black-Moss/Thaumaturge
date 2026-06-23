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

public final class CardNotation extends TheorycraftCard {
    private @Nullable ResourceKey<IResearchCategory> from;
    private @Nullable ResourceKey<IResearchCategory> to;

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
        return Component.translatable("card.thaumcraft.notation.name");
    }

    @Override
    public Component description() {
        return Component.translatable(
                "card.thaumcraft.notation.text",
                from == null ? Component.empty() : CategoryComponents.emphasised(from),
                to == null ? Component.empty() : CategoryComponents.emphasised(to));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        if (data.categoriesWithTotal().size() < 2) return false;
        int low = Integer.MAX_VALUE;
        int high = 0;
        ResourceKey<IResearchCategory> lowKey = null;
        ResourceKey<IResearchCategory> highKey = null;
        for (ResourceKey<IResearchCategory> key : data.categoriesWithTotal()) {
            int value = data.categoryTotal(key);
            if (value < low) {
                low = value;
                lowKey = key;
            }
            if (value > high) {
                high = value;
                highKey = key;
            }
        }
        if (lowKey == null || highKey == null || lowKey == highKey || low <= 0) return false;
        from = lowKey;
        to = highKey;
        return true;
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (from == null || to == null) return false;
        int moved = data.categoryTotal(from);
        if (moved <= 0) return false;
        data.addCategoryTotal(from, -moved);
        int gained = moved / 2 + player.getRandom().nextInt(Math.max(1, moved / 2 + 1));
        data.addCategoryTotal(to, gained);
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        if (from != null) output.store("from", ResourceKey.codec(IResearchCategory.REGISTRY_KEY), from);
        if (to != null) output.store("to", ResourceKey.codec(IResearchCategory.REGISTRY_KEY), to);
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        from = input.read("from", ResourceKey.codec(IResearchCategory.REGISTRY_KEY)).orElse(null);
        to = input.read("to", ResourceKey.codec(IResearchCategory.REGISTRY_KEY)).orElse(null);
    }
}
