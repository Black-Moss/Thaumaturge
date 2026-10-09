package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.ResearchUnlockConditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

final class BrowserModel {
    private final Player player;
    private final List<CategoryRef> builtIn = new ArrayList<>();
    private final List<CategoryRef> addOn = new ArrayList<>();
    private final Map<Identifier, EntryNode> entries = new LinkedHashMap<>();
    private final Map<Identifier, List<EntryNode>> entriesByCategory = new HashMap<>();
    private final Map<Identifier, Boolean> visibilityCache = new HashMap<>();

    BrowserModel(Player player) {
        this.player = player;
        RegistryAccess access = player.registryAccess();
        elements(access, IResearchCategory.REGISTRY_KEY).forEach(ref -> {
            CategoryRef category = new CategoryRef(ref.key().identifier(), ref.value());
            (TTIds.MODID.equals(category.id().getNamespace()) ? builtIn : addOn).add(category);
        });
        builtIn.sort(Comparator.comparingInt(category -> category.data().index()));
        elements(access, IResearchEntry.REGISTRY_KEY).forEach(ref -> {
            Optional<ResourceKey<IResearchCategory>> categoryKey = ref.value().category().unwrapKey();
            if (categoryKey.isPresent()) {
                EntryNode node = new EntryNode(ref.key().identifier(), ref, categoryKey.get().identifier());
                entries.put(node.id(), node);
                entriesByCategory.computeIfAbsent(node.categoryId(), id -> new ArrayList<>()).add(node);
            }
        });
    }

    private static <T> Stream<Holder.Reference<T>> elements(RegistryAccess access, ResourceKey<Registry<T>> key) {
        return access.lookup(key).map(lookup -> lookup.listElements()).orElseGet(Stream::empty);
    }

    Player player() {
        return player;
    }

    IPlayerKnowledge knowledge() {
        return KnowledgeAccess.of(player);
    }

    List<CategoryRef> builtIn() {
        return builtIn;
    }

    List<CategoryRef> addOn() {
        return addOn;
    }

    Collection<EntryNode> allEntries() {
        return entries.values();
    }

    @Nullable
    EntryNode entry(Identifier id) {
        return entries.get(id);
    }

    List<EntryNode> entriesOf(Identifier categoryId) {
        return entriesByCategory.getOrDefault(categoryId, List.of());
    }

    @Nullable
    CategoryRef category(@Nullable Identifier id) {
        if (id == null) {
            return null;
        }
        for (CategoryRef category : builtIn) {
            if (category.id().equals(id)) {
                return category;
            }
        }
        for (CategoryRef category : addOn) {
            if (category.id().equals(id)) {
                return category;
            }
        }
        return null;
    }

    boolean shown(CategoryRef category) {
        IPlayerKnowledge knowledge = knowledge();
        return category.data().requiredResearch().map(knowledge::isResearchComplete).orElse(true);
    }

    List<CategoryRef> shownBuiltIn() {
        return builtIn.stream().filter(this::shown).toList();
    }

    List<CategoryRef> shownAddOn() {
        return addOn.stream().filter(this::shown).toList();
    }

    void clearVisibilityCache() {
        visibilityCache.clear();
    }

    boolean canUnlock(EntryNode node) {
        IPlayerKnowledge knowledge = knowledge();
        for (ResearchParent parent : node.value().parents()) {
            if (!parent.isSatisfiedBy(knowledge)) {
                return false;
            }
        }
        return knowledge.isResearchKnown(node.id()) || ResearchUnlockConditions.passes(player, knowledge, node.id());
    }

    boolean visible(EntryNode node) {
        Boolean cached = visibilityCache.get(node.id());
        if (cached != null) {
            return cached;
        }
        visibilityCache.put(node.id(), Boolean.TRUE);
        boolean result = evaluateVisible(node);
        visibilityCache.put(node.id(), result);
        return result;
    }

    private boolean evaluateVisible(EntryNode node) {
        if (knowledge().isResearchKnown(node.id())) {
            return true;
        }
        IResearchEntry entry = node.value();
        if (entry.hasMeta(ResearchEntryMeta.HIDDEN) && (entry.parents().isEmpty() || !canUnlock(node))) {
            return false;
        }
        for (ResearchParent parent : entry.parents()) {
            EntryNode parentNode = entries.get(parent.id());
            if (parentNode != null && !visible(parentNode)) {
                return false;
            }
        }
        return true;
    }

    record CategoryRef(Identifier id, IResearchCategory data) {
    }

    record EntryNode(Identifier id, Holder.Reference<IResearchEntry> holder, Identifier categoryId) {
        IResearchEntry value() {
            return holder.value();
        }
    }
}
