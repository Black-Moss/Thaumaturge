package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public final class ResearchProgression {

    private ResearchProgression() {}

    public static void reset(ServerPlayer player) {
        KnowledgeAccess.of(player).clear();
        ResearchManager.applyAutoUnlock(player);
    }

    public static int grant(ServerPlayer player, Identifier research) {
        return completeAll(player, prerequisiteClosure(player, research));
    }

    public static int prepare(ServerPlayer player, Identifier research) {
        Set<Identifier> prerequisites = prerequisiteClosure(player, research);
        prerequisites.remove(research);
        int completed = completeAll(player, prerequisites);
        forgetAndResync(player, research);
        ResearchManager.unlock(player, research);
        return completed;
    }

    public static int revoke(ServerPlayer player, Identifier research) {
        Map<Identifier, List<Identifier>> dependents = new HashMap<>();
        for (Holder.Reference<IResearchEntry> holder : entries(player).listElements().toList()) {
            Identifier dependent = holder.key().identifier();
            for (Identifier requirement : requirements(holder.value())) {
                dependents.computeIfAbsent(requirement, key -> new ArrayList<>()).add(dependent);
            }
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        int removed = 0;
        for (Identifier id : transitiveDependents(research, dependents)) {
            if (knowledge.removeResearch(id) && !id.equals(research)) {
                removed++;
            }
        }
        knowledge.sync(player);
        return removed;
    }

    private static void forgetAndResync(ServerPlayer player, Identifier research) {
        Optional.of(KnowledgeAccess.of(player)).filter(knowledge -> knowledge.removeResearch(research)).ifPresent(knowledge -> knowledge.sync(player));
    }

    public static int completeCategory(ServerPlayer player, ResourceKey<IResearchCategory> category) {
        return completeAll(player, closureOfMatching(player, entry -> entry.category().is(category), List.of()));
    }

    public static int setStage(ServerPlayer player, Holder<IResearchCategory> stage) {
        reset(player);
        int index = stage.value().index();
        List<Identifier> gate = stage.value().requiredResearch().stream().toList();
        return completeAll(player, closureOfMatching(player, entry -> entry.category().value().index() < index, gate));
    }

    private static HolderLookup.RegistryLookup<IResearchEntry> entries(ServerPlayer player) {
        return player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY);
    }

    private static Set<Identifier> prerequisiteClosure(ServerPlayer player, Identifier research) {
        DependencyWalk walk = new DependencyWalk(entries(player));
        walk.visit(research);
        return walk.ordered;
    }

    private static Set<Identifier> closureOfMatching(ServerPlayer player, Predicate<IResearchEntry> filter, List<Identifier> extraRoots) {
        HolderLookup.RegistryLookup<IResearchEntry> entries = entries(player);
        List<Identifier> roots = entries.listElements().filter(holder -> filter.test(holder.value())).map(holder -> holder.key().identifier()).toList();
        DependencyWalk walk = new DependencyWalk(entries);
        Stream.concat(roots.stream(), extraRoots.stream()).forEach(walk::visit);
        return walk.ordered;
    }

    private static Set<Identifier> transitiveDependents(Identifier root, Map<Identifier, List<Identifier>> dependents) {
        Set<Identifier> found = new LinkedHashSet<>();
        Deque<Identifier> pending = new ArrayDeque<>(List.of(root));
        while (!pending.isEmpty()) {
            Identifier current = pending.removeLast();
            if (found.add(current)) {
                pending.addAll(dependents.getOrDefault(current, List.of()));
            }
        }
        return found;
    }

    private static List<Identifier> requirements(IResearchEntry entry) {
        Stream<Identifier> category = entry.category().value().requiredResearch().stream();
        Stream<Identifier> parents = entry.parents().stream().map(ResearchParent::id);
        Stream<Identifier> stages = entry.stages().stream().map(IResearchStage::requiredResearch).flatMap(Collection::stream);
        return Stream.of(category, parents, stages).flatMap(part -> part).toList();
    }

    private static int completeAll(ServerPlayer player, Collection<Identifier> research) {
        return research.stream().mapToInt(id -> ResearchManager.complete(player, id) ? 1 : 0).sum();
    }

    private static final class DependencyWalk {
        private final Set<Identifier> ordered = new LinkedHashSet<>();
        private final Set<Identifier> seen = new HashSet<>();
        private final HolderLookup.RegistryLookup<IResearchEntry> entries;

        private DependencyWalk(HolderLookup.RegistryLookup<IResearchEntry> entries) {
            this.entries = entries;
        }

        private void visit(Identifier research) {
            if (seen.add(research)) {
                entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).ifPresent(entry -> requirements(entry.value()).forEach(this::visit));
                ordered.add(research);
            }
        }
    }
}
