package com.leclowndu93150.thaumcraft.content.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.CardFactory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import com.leclowndu93150.thaumcraft.registry.TCTheorycraft;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TheorycraftManager {
    public static final int DEFAULT_HAND_SIZE = 2;
    public static final int MAX_HAND_SIZE = 3;
    public static final int BASE_INSPIRATION = 5;

    private TheorycraftManager() {}

    public static void beginSession(ServerPlayer player, ServerLevel level, BlockPos tablePos) {
        beginSession(player, level, tablePos, Set.of());
    }

    public static void beginSession(ServerPlayer player, ServerLevel level, BlockPos tablePos, Set<ResourceKey<ITheorycraftAid>> requestedAids) {
        ResearchTableData data = player.getData(TCAttachments.RESEARCH_TABLE);
        data.reset();
        int inspiration = computeStartingInspiration(player);
        Set<ResourceKey<ITheorycraftAid>> matchedAids = scanAids(level, tablePos, player.registryAccess());
        Set<ResourceKey<ITheorycraftAid>> chosenAids = new HashSet<>();
        int aidBudget = Math.max(0, inspiration - 1);
        for (ResourceKey<ITheorycraftAid> requested : requestedAids) {
            if (chosenAids.size() >= aidBudget) break;
            if (matchedAids.contains(requested)) {
                chosenAids.add(requested);
            }
        }
        data.setInspirationStart(inspiration);
        data.setInspiration(inspiration);
        data.setAidsChosen(chosenAids.size());
        for (ResourceKey<ITheorycraftAid> aidKey : chosenAids) {
            player.registryAccess().lookup(TCTheorycraft.AIDS_REGISTRY_KEY)
                    .flatMap(lookup -> lookup.get(aidKey))
                    .ifPresent(aidHolder -> {
                        for (ResourceKey<CardFactory> cardKey : aidHolder.value().cards()) {
                            data.addAidCard(cardKey);
                        }
                    });
        }
        drawHand(player, data, DEFAULT_HAND_SIZE);
        data.sync(player);
    }

    public static void drawHand(ServerPlayer player, ResearchTableData data, int targetSize) {
        int draw = targetSize;
        if (draw == DEFAULT_HAND_SIZE && data.bonusDraws() > 0) {
            data.setBonusDraws(data.bonusDraws() - 1);
            draw++;
        }
        data.clearCardChoices();
        List<ResourceKey<IResearchCategory>> available = data.availableCategories(player);
        List<ResourceKey<CardFactory>> deck = new ArrayList<>();
        player.registryAccess().lookup(TCTheorycraft.CARDS_REGISTRY_KEY).ifPresent(lookup ->
                lookup.listElements().forEach(holder -> holder.unwrapKey().ifPresent(deck::add)));
        Set<ResourceKey<CardFactory>> taken = new HashSet<>();
        int safety = 0;
        boolean aidDrawnThisRound = false;
        while (draw > 0 && safety < 1000) {
            safety++;
            boolean useAid = !aidDrawnThisRound && !data.aidCards().isEmpty() && player.getRandom().nextFloat() <= 0.25F;
            if (useAid) {
                List<ResourceKey<CardFactory>> aids = data.aidCards();
                ResourceKey<CardFactory> chosen = aids.get(player.getRandom().nextInt(aids.size()));
                TheorycraftCard card = hydrate(player, chosen);
                if (card == null || card.inspirationCost() > data.inspiration()
                        || (card.category() != null && data.isCategoryBlocked(card.category()))
                        || taken.contains(chosen)) {
                    continue;
                }
                taken.add(chosen);
                data.addCardChoice(ResearchTableData.CardChoice.snapshot(chosen, card, true, false, player.registryAccess()));
                data.removeAidCard(chosen);
                aidDrawnThisRound = true;
            } else if (!deck.isEmpty()) {
                ResourceKey<CardFactory> chosen = deck.get(player.getRandom().nextInt(deck.size()));
                if (taken.contains(chosen)) continue;
                TheorycraftCard card = hydrate(player, chosen);
                if (card == null || card.isAidOnly() || card.inspirationCost() > data.inspiration()) continue;
                if (card.category() != null && !available.contains(card.category())) continue;
                taken.add(chosen);
                data.addCardChoice(ResearchTableData.CardChoice.snapshot(chosen, card, false, false, player.registryAccess()));
            } else {
                break;
            }
            draw--;
        }
    }

    public static boolean playCard(ServerPlayer player, int choiceIndex) {
        ResearchTableData data = player.getData(TCAttachments.RESEARCH_TABLE);
        List<ResearchTableData.CardChoice> choices = data.cardChoices();
        if (choiceIndex < 0 || choiceIndex >= choices.size()) return false;
        if (anyCardSelected(choices)) return false;
        ResearchTableData.CardChoice choice = choices.get(choiceIndex);
        TheorycraftCard card = choice.hydrate(player.registryAccess());
        if (card == null) return false;
        if (card.inspirationCost() > data.inspiration()) return false;
        if (!card.activate(player, data)) return false;
        data.setInspiration(data.inspiration() - card.inspirationCost());
        data.setPlacedCards(data.placedCards() + 1);
        data.replaceCardChoice(choiceIndex, choice.withSelected(true));
        data.sync(player);
        return true;
    }

    public static void completeCardAnimation(ServerPlayer player) {
        ResearchTableData data = player.getData(TCAttachments.RESEARCH_TABLE);
        List<ResearchTableData.CardChoice> choices = data.cardChoices();
        if (!anyCardSelected(choices)) return;
        ResearchTableData.CardChoice selected = null;
        for (ResearchTableData.CardChoice choice : choices) {
            if (choice.selected()) {
                selected = choice;
                break;
            }
        }
        if (selected == null) return;
        if (data.lastDraw() != null) {
            data.addSavedCard(data.lastDraw().seed());
        }
        data.setLastDraw(selected);
        drawHand(player, data, DEFAULT_HAND_SIZE);
        data.sync(player);
    }

    private static boolean anyCardSelected(List<ResearchTableData.CardChoice> choices) {
        for (ResearchTableData.CardChoice choice : choices) {
            if (choice.selected()) return true;
        }
        return false;
    }

    public static void endSession(ServerPlayer player) {
        ResearchTableData data = player.getData(TCAttachments.RESEARCH_TABLE);
        data.reset();
        data.sync(player);
    }

    private static int computeStartingInspiration(ServerPlayer player) {
        return computeInspirationFor(player);
    }

    public static int computeInspirationFor(Player player) {
        float[] total = { BASE_INSPIRATION };
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).ifPresent(lookup -> {
            for (Identifier id : knowledge.researchList()) {
                lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, id)).ifPresent(ref -> {
                    IResearchEntry entry = ref.value();
                    if (entry.hasMeta(ResearchEntryMeta.SPIKY)) total[0] += 0.5F;
                    if (entry.hasMeta(ResearchEntryMeta.HIDDEN)) total[0] += 0.1F;
                });
            }
        });
        return Math.min(15, Math.round(total[0]));
    }

    private static Set<ResourceKey<ITheorycraftAid>> scanAids(ServerLevel level, BlockPos tablePos, RegistryAccess access) {
        Set<ResourceKey<ITheorycraftAid>> matched = new HashSet<>();
        access.lookup(TCTheorycraft.AIDS_REGISTRY_KEY).ifPresent(lookup ->
                lookup.listElements().forEach(holder -> {
                    if (holder.value().matches(level, tablePos)) {
                        holder.unwrapKey().ifPresent(matched::add);
                    }
                }));
        return matched;
    }

    private static TheorycraftCard hydrate(ServerPlayer player, ResourceKey<CardFactory> key) {
        TheorycraftCard card = player.registryAccess()
                .lookup(TCTheorycraft.CARDS_REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(key))
                .map(holder -> holder.value().create())
                .orElse(null);
        if (card == null) return null;
        card.setSeed(player.getRandom().nextLong());
        ResearchTableData data = player.getData(TCAttachments.RESEARCH_TABLE);
        if (!card.initialize(player, data)) return null;
        return card;
    }
}
