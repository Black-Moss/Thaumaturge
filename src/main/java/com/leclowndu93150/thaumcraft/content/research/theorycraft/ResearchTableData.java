package com.leclowndu93150.thaumcraft.content.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.CardFactory;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.IResearchTableData;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import com.leclowndu93150.thaumcraft.registry.TCTheorycraft;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumcraft.Thaumcraft;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;

public final class ResearchTableData implements IResearchTableData {
    public static final MapCodec<ResearchTableData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("inspiration", 0).forGetter(d -> d.inspiration),
            Codec.INT.optionalFieldOf("inspiration_start", 0).forGetter(d -> d.inspirationStart),
            Codec.INT.optionalFieldOf("bonus_draws", 0).forGetter(d -> d.bonusDraws),
            Codec.INT.optionalFieldOf("placed_cards", 0).forGetter(d -> d.placedCards),
            Codec.INT.optionalFieldOf("aids_chosen", 0).forGetter(d -> d.aidsChosen),
            Codec.INT.optionalFieldOf("penalty_start", 0).forGetter(d -> d.penaltyStart),
            Codec.LONG.listOf().optionalFieldOf("saved_cards", List.of()).forGetter(d -> List.copyOf(d.savedCards)),
            ResourceKey.codec(TCTheorycraft.CARDS_REGISTRY_KEY).listOf().optionalFieldOf("aid_cards", List.of()).forGetter(d -> List.copyOf(d.aidCards)),
            ResourceKey.codec(IResearchCategory.REGISTRY_KEY).listOf().optionalFieldOf("blocked_categories", List.of()).forGetter(d -> List.copyOf(d.categoriesBlocked)),
            CategoryTotalEntry.CODEC.listOf().optionalFieldOf("category_totals", List.of()).forGetter(ResearchTableData::categoryTotalsAsList),
            CardChoice.CODEC.listOf().optionalFieldOf("card_choices", List.of()).forGetter(d -> List.copyOf(d.cardChoices)),
            CardChoice.CODEC.optionalFieldOf("last_draw").forGetter(d -> Optional.ofNullable(d.lastDraw))
    ).apply(instance, ResearchTableData::fromRecord));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchTableData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private int inspiration;
    private int inspirationStart;
    private int bonusDraws;
    private int placedCards;
    private int aidsChosen;
    private int penaltyStart;

    private final List<Long> savedCards = new ArrayList<>();
    private final List<ResourceKey<CardFactory>> aidCards = new ArrayList<>();
    private final List<ResourceKey<IResearchCategory>> categoriesBlocked = new ArrayList<>();
    private final TreeMap<ResourceKey<IResearchCategory>, Integer> categoryTotals =
            new TreeMap<>((a, b) -> a.identifier().compareTo(b.identifier()));
    private final List<CardChoice> cardChoices = new ArrayList<>();
    private @Nullable CardChoice lastDraw;

    public ResearchTableData() {
    }

    private static ResearchTableData fromRecord(
            int inspiration,
            int inspirationStart,
            int bonusDraws,
            int placedCards,
            int aidsChosen,
            int penaltyStart,
            List<Long> savedCards,
            List<ResourceKey<CardFactory>> aidCards,
            List<ResourceKey<IResearchCategory>> blockedCategories,
            List<CategoryTotalEntry> totals,
            List<CardChoice> choices,
            Optional<CardChoice> lastDraw
    ) {
        ResearchTableData data = new ResearchTableData();
        data.inspiration = inspiration;
        data.inspirationStart = inspirationStart;
        data.bonusDraws = bonusDraws;
        data.placedCards = placedCards;
        data.aidsChosen = aidsChosen;
        data.penaltyStart = penaltyStart;
        data.savedCards.addAll(savedCards);
        data.aidCards.addAll(aidCards);
        data.categoriesBlocked.addAll(blockedCategories);
        for (CategoryTotalEntry entry : totals) {
            data.categoryTotals.put(entry.category(), entry.amount());
        }
        data.cardChoices.addAll(choices);
        data.lastDraw = lastDraw.orElse(null);
        return data;
    }

    private List<CategoryTotalEntry> categoryTotalsAsList() {
        List<CategoryTotalEntry> entries = new ArrayList<>(categoryTotals.size());
        categoryTotals.forEach((cat, amt) -> entries.add(new CategoryTotalEntry(cat, amt)));
        return entries;
    }

    @Override
    public int inspiration() {
        return inspiration;
    }

    @Override
    public int inspirationStart() {
        return inspirationStart;
    }

    public int bonusDraws() {
        return bonusDraws;
    }

    public int placedCards() {
        return placedCards;
    }

    public int aidsChosen() {
        return aidsChosen;
    }

    public int penaltyStart() {
        return penaltyStart;
    }

    public List<Long> savedCards() {
        return List.copyOf(savedCards);
    }

    public List<ResourceKey<CardFactory>> aidCards() {
        return List.copyOf(aidCards);
    }

    public List<CardChoice> cardChoices() {
        return List.copyOf(cardChoices);
    }

    public @Nullable CardChoice lastDraw() {
        return lastDraw;
    }

    public boolean isComplete() {
        return inspiration <= 0;
    }

    public boolean hasTotal(ResourceKey<IResearchCategory> category) {
        return categoryTotals.containsKey(category);
    }

    @Override
    public int categoryTotal(ResourceKey<IResearchCategory> category) {
        Integer value = categoryTotals.get(category);
        return value == null ? 0 : value;
    }

    @Override
    public void addCategoryTotal(ResourceKey<IResearchCategory> category, int amount) {
        if (category == null) return;
        int current = categoryTotals.getOrDefault(category, 0) + amount;
        if (current <= 0) {
            categoryTotals.remove(category);
        } else {
            categoryTotals.put(category, current);
        }
    }

    @Override
    public void addInspiration(int amount) {
        inspiration = Math.min(inspirationStart, inspiration + amount);
        if (inspiration < 0) inspiration = 0;
    }

    @Override
    public List<ResourceKey<IResearchCategory>> availableCategories(Player player) {
        List<ResourceKey<IResearchCategory>> result = new ArrayList<>();
        player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY).ifPresent(lookup ->
                lookup.listElements().forEach(holder -> holder.unwrapKey().ifPresent(key -> {
                    if (isCategoryBlocked(key)) return;
                    holder.value().requiredResearch().ifPresentOrElse(
                            required -> {
                                if (KnowledgeAccess.of(player).isResearchKnown(required)) {
                                    result.add(key);
                                }
                            },
                            () -> result.add(key)
                    );
                })));
        return result;
    }

    @Override
    public boolean isCategoryBlocked(ResourceKey<IResearchCategory> category) {
        return categoriesBlocked.contains(category);
    }

    @Override
    public void blockCategory(ResourceKey<IResearchCategory> category) {
        if (category != null && !categoriesBlocked.contains(category)) {
            categoriesBlocked.add(category);
        }
    }

    @Override
    public void addBonusDraws(int amount) {
        bonusDraws = Math.max(0, bonusDraws + amount);
    }

    @Override
    public Set<ResourceKey<IResearchCategory>> categoriesWithTotal() {
        return new LinkedHashSet<>(categoryTotals.keySet());
    }

    @Override
    public int totalUnblockedPoints() {
        int total = 0;
        for (var entry : categoryTotals.entrySet()) {
            if (!categoriesBlocked.contains(entry.getKey())) {
                total += entry.getValue();
            }
        }
        return total;
    }

    @Override
    public void setCategoryTotal(ResourceKey<IResearchCategory> category, int amount) {
        if (category == null) return;
        if (amount <= 0) {
            categoryTotals.remove(category);
        } else {
            categoryTotals.put(category, amount);
        }
    }

    @Override
    public void incrementPenalty() {
        penaltyStart++;
    }

    public void setInspiration(int value) {
        this.inspiration = Math.max(0, value);
    }

    public void setInspirationStart(int value) {
        this.inspirationStart = Math.max(0, value);
    }

    public void setBonusDraws(int value) {
        this.bonusDraws = Math.max(0, value);
    }

    public void setPlacedCards(int value) {
        this.placedCards = Math.max(0, value);
    }

    public void setAidsChosen(int value) {
        this.aidsChosen = Math.max(0, value);
    }

    public void setPenaltyStart(int value) {
        this.penaltyStart = Math.max(0, value);
    }

    public void addSavedCard(long seed) {
        savedCards.add(seed);
    }

    public boolean removeSavedCard(long seed) {
        return savedCards.remove(Long.valueOf(seed));
    }

    public void clearSavedCards() {
        savedCards.clear();
    }

    public void addAidCard(ResourceKey<CardFactory> key) {
        aidCards.add(key);
    }

    public boolean removeAidCard(ResourceKey<CardFactory> key) {
        return aidCards.remove(key);
    }

    public void clearAidCards() {
        aidCards.clear();
    }

    public void clearCardChoices() {
        cardChoices.clear();
    }

    public void addCardChoice(CardChoice choice) {
        cardChoices.add(choice);
    }

    public void replaceCardChoice(int index, CardChoice choice) {
        cardChoices.set(index, choice);
    }

    public void setLastDraw(@Nullable CardChoice choice) {
        this.lastDraw = choice;
    }

    public void sync(ServerPlayer player) {
        player.syncData(TCAttachments.RESEARCH_TABLE);
    }

    public void reset() {
        inspiration = 0;
        inspirationStart = 0;
        bonusDraws = 0;
        placedCards = 0;
        aidsChosen = 0;
        penaltyStart = 0;
        savedCards.clear();
        aidCards.clear();
        categoriesBlocked.clear();
        categoryTotals.clear();
        cardChoices.clear();
        lastDraw = null;
    }

    private record CategoryTotalEntry(ResourceKey<IResearchCategory> category, int amount) {
        static final Codec<CategoryTotalEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(IResearchCategory.REGISTRY_KEY).fieldOf("category").forGetter(CategoryTotalEntry::category),
                Codec.INT.fieldOf("amount").forGetter(CategoryTotalEntry::amount)
        ).apply(instance, CategoryTotalEntry::new));
    }

    public record CardChoice(
            ResourceKey<CardFactory> key,
            long seed,
            CardState state,
            boolean fromAid,
            boolean selected
    ) {
        public static final Codec<CardChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(TCTheorycraft.CARDS_REGISTRY_KEY).fieldOf("key").forGetter(CardChoice::key),
                Codec.LONG.fieldOf("seed").forGetter(CardChoice::seed),
                CardState.CODEC.optionalFieldOf("state", CardState.EMPTY).forGetter(CardChoice::state),
                Codec.BOOL.optionalFieldOf("from_aid", false).forGetter(CardChoice::fromAid),
                Codec.BOOL.optionalFieldOf("selected", false).forGetter(CardChoice::selected)
        ).apply(instance, CardChoice::new));

        public CardChoice withSelected(boolean selected) {
            return new CardChoice(key, seed, state, fromAid, selected);
        }

        public CardChoice withState(CardState state) {
            return new CardChoice(key, seed, state, fromAid, selected);
        }

        public @Nullable TheorycraftCard hydrate(HolderLookup.Provider registries) {
            TheorycraftCard card = registries
                    .lookup(TCTheorycraft.CARDS_REGISTRY_KEY)
                    .flatMap(lookup -> lookup.get(key))
                    .map(holder -> holder.value().create())
                    .orElse(null);
            if (card == null) return null;
            card.setSeed(seed);
            CompoundTag stored = state.tag();
            if (stored != null && !stored.isEmpty()) {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(() -> "card:" + key.identifier(), Thaumcraft.LOGGER)) {
                    card.read(TagValueInput.create(reporter, registries, stored), registries);
                }
            }
            return card;
        }

        public static CardChoice snapshot(
                ResourceKey<CardFactory> key,
                TheorycraftCard card,
                boolean fromAid,
                boolean selected,
                HolderLookup.Provider registries
        ) {
            CardState state;
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(() -> "card:" + key.identifier(), Thaumcraft.LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
                card.write(output, registries);
                state = new CardState(output.buildResult());
            }
            return new CardChoice(key, card.seed(), state, fromAid, selected);
        }
    }

    public record CardState(@Nullable CompoundTag tag) {
        public static final CardState EMPTY = new CardState(null);

        public static final Codec<CardState> CODEC = CompoundTag.CODEC
                .xmap(CardState::new, state -> state.tag == null ? new CompoundTag() : state.tag);
    }

    public Set<ResourceKey<IResearchCategory>> allCategoryKeys() {
        return new LinkedHashSet<>(categoryTotals.keySet());
    }
}
