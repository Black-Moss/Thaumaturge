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

public final class CardReactions extends TheorycraftCard {
    private static final float INSPIRATION_REFUND_CHANCE = 0.33F;

    private @Nullable Holder<IAspect> aspect1;
    private @Nullable Holder<IAspect> aspect2;

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
        return Component.translatable("card.thaumcraft.reactions.name");
    }

    @Override
    public Component description() {
        if (aspect1 == null || aspect2 == null) {
            return Component.translatable("card.thaumcraft.reactions.text", Component.empty(), Component.empty());
        }
        return Component.translatable("card.thaumcraft.reactions.text",
                CardHelper.boldAspectName(aspect1), CardHelper.boldAspectName(aspect2));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        Random rng = new Random(seed());
        List<Holder.Reference<IAspect>> compounds = CardHelper.compoundAspects(player.registryAccess());
        int first = rng.nextInt(compounds.size());
        aspect1 = compounds.get(first);
        int second = first;
        while (second == first) {
            second = rng.nextInt(compounds.size());
        }
        aspect2 = compounds.get(second);
        return true;
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        if (aspect1 == null || aspect2 == null) return List.of();
        return List.of(new CardItemRequirement(CardHelper.crystal(aspect1), false),
                new CardItemRequirement(CardHelper.crystal(aspect2), false));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        data.addCategoryTotal(TCResearchCategories.ALCHEMY, 25);
        if (player.getRandom().nextFloat() < INSPIRATION_REFUND_CHANCE) {
            data.addInspiration(1);
        }
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        saveAspect(output, "aspect1", aspect1);
        saveAspect(output, "aspect2", aspect2);
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        aspect1 = loadAspect(input, registries, "aspect1");
        aspect2 = loadAspect(input, registries, "aspect2");
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
