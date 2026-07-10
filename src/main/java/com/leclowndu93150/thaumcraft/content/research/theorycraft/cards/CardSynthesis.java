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
import net.minecraft.world.item.ItemStack;

public final class CardSynthesis extends TheorycraftCard {
    private static final float INSPIRATION_REFUND_CHANCE = 0.33F;

    private @Nullable Holder<IAspect> aspect1;
    private @Nullable Holder<IAspect> aspect2;
    private @Nullable Holder<IAspect> aspect3;

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
        return Component.translatable("card.thaumcraft.synthesis.name");
    }

    @Override
    public Component description() {
        if (aspect1 == null || aspect2 == null) {
            return Component.translatable("card.thaumcraft.synthesis.text", Component.empty(), Component.empty());
        }
        return Component.translatable("card.thaumcraft.synthesis.text",
                CardHelper.boldAspectName(aspect1), CardHelper.boldAspectName(aspect2));
    }

    @Override
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        Random rng = new Random(seed());
        List<Holder.Reference<IAspect>> compounds = CardHelper.compoundAspects(player.registryAccess());
        aspect3 = compounds.get(rng.nextInt(compounds.size()));
        List<Holder<IAspect>> components = aspect3.value().components();
        aspect1 = components.get(0);
        aspect2 = components.get(1);
        return true;
    }

    @Override
    public List<CardItemRequirement> requiredItems() {
        if (aspect1 == null || aspect2 == null) return List.of();
        return List.of(new CardItemRequirement(CardHelper.crystal(aspect1), true),
                new CardItemRequirement(CardHelper.crystal(aspect2), true));
    }

    @Override
    public boolean activate(ServerPlayer player, IResearchTableData data) {
        if (aspect3 == null) return false;
        data.addCategoryTotal(TCResearchCategories.ALCHEMY, 40);
        if (player.getRandom().nextFloat() < INSPIRATION_REFUND_CHANCE) {
            data.addInspiration(1);
        }
        ItemStack result = CardHelper.crystal(aspect3);
        if (!player.getInventory().add(result)) {
            player.drop(result, true);
        }
        return true;
    }

    @Override
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        super.write(output, registries);
        saveAspect(output, "aspect1", aspect1);
        saveAspect(output, "aspect2", aspect2);
        saveAspect(output, "aspect3", aspect3);
    }

    @Override
    public void read(ValueInput input, HolderLookup.Provider registries) {
        super.read(input, registries);
        aspect1 = loadAspect(input, registries, "aspect1");
        aspect2 = loadAspect(input, registries, "aspect2");
        aspect3 = loadAspect(input, registries, "aspect3");
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
