package com.leclowndu93150.thaumcraft.content.research.theorycraft.cards;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CardConcentrate extends TheorycraftCard {
    private static final float INSPIRATION_REFUND_CHANCE = 0.33F;

    private @Nullable Holder<IAspect> aspect;

    @Override
    public int inspirationCost() {
        return 1;
    }

    @Override
    public ResourceKey<IResearchCategory> category() {
        return TCResearchCategories.ALCHEMY;
    }

    @Override
    public Component name() {
        return Component.translatable("card.thaumcraft.concentrate.name");
    }

    @Override
    public Component description() {
        return aspect == null
                ? Component.translatable("card.thaumcraft.concentrate.text", Component.empty())
                : Component.translatable("card.thaumcraft.concentrate.text", CardHelper.boldAspectName(aspect));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        aspect = CardHelper.randomCompound(player.registryAccess(), new Random(seed()));
        return aspect != null;
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        return aspect == null ? List.of() : List.of(new CardItemRequirement(CardHelper.crystal(aspect), false));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.ALCHEMY, 15);
        data.addBonusDraws(1);
        if (player.getRandom().nextFloat() < INSPIRATION_REFUND_CHANCE) {
            data.addInspiration(1);
        }
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        saveAspect(output, "aspect", aspect);
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        aspect = loadAspect(input, registries, "aspect");
    }

    private static @Nullable Holder<IAspect> loadAspect(ValueInput input, HolderLookup.Provider registries, String field) {
        return input.read(field, Identifier.CODEC)
                .flatMap(id -> registries.lookupOrThrow(IAspect.REGISTRY_KEY)
                        .get(ResourceKey.create(IAspect.REGISTRY_KEY, id)))
                .map(holder -> (Holder<IAspect>) holder)
                .orElse(null);
    }

    private static void saveAspect(ValueOutput output, String field, @Nullable Holder<IAspect> aspect) {
        if (aspect != null) {
            aspect.unwrapKey().ifPresent(key -> output.store(field, Identifier.CODEC, key.identifier()));
        }
    }
}
