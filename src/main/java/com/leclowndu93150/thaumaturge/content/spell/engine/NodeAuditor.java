package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

final class NodeAuditor {
    private final HolderLookup.Provider registries;
    private final @Nullable Player player;
    private final SpellProblemLog log;
    private final SpellTally tally;

    NodeAuditor(HolderLookup.Provider registries, @Nullable Player player, SpellProblemLog log, SpellTally tally) {
        this.registries = registries;
        this.player = player;
        this.log = log;
        this.tally = tally;
    }

    void auditBelow(SpellNode root) {
        Deque<SpellNode> pending = new ArrayDeque<>();
        stack(pending, root);
        while (!pending.isEmpty()) {
            SpellNode node = pending.pop();
            if (audit(node)) {
                stack(pending, node);
            }
        }
    }

    private static void stack(Deque<SpellNode> pending, SpellNode parent) {
        List<SpellNode> children = parent.children();
        for (int index = children.size() - 1; index >= 0; index--) {
            pending.push(children.get(index));
        }
    }

    private boolean audit(SpellNode node) {
        Optional<SpellPart> found = Spells.part(registries, node.part());
        if (found.isEmpty()) {
            log.blockerAt(node, "unknown_part", node.part().identifier().toString());
            return false;
        }
        SpellPart part = found.get();
        checkPlacement(node, part);
        if (part.kind() == SpellPartKind.EFFECT) {
            tally.markEffect();
        }
        int uses = tally.countUse(node.part());
        checkUseLimit(node, part, uses);
        checkPartResearch(node, part);
        checkAspect(node, part);
        tally.addCost(SpellAnalyzer.nodeComplexity(node, part, registries), uses, SpellAnalyzer.nodeVis(node, part, registries));
        tally.scale(part.visMultiplier(), part.cooldownMultiplier());
        checkShape(node, part);
        return true;
    }

    private void checkPlacement(SpellNode node, SpellPart part) {
        if (node.part().equals(Spell.ORIGIN)) {
            log.blockerAt(node, "misplaced_origin");
        } else if (part.hidden()) {
            log.blockerAt(node, "hidden_part", SpellText.partName(node.part()));
        }
    }

    private void checkUseLimit(SpellNode node, SpellPart part, int uses) {
        int max = part.maxPerSpell();
        if (max > 0 && uses == max + 1) {
            log.blockerAt(node, "repeated", SpellText.partName(node.part()), max);
        }
    }

    private void checkPartResearch(SpellNode node, SpellPart part) {
        if (player == null || part.research().isEmpty()) {
            return;
        }
        if (!ResearchGate.passes(player, part.research().get())) {
            log.blockerAt(node, "part_locked", SpellText.partName(node.part()), SpellText.researchTitle(part.research().get().entry()));
        }
    }

    private void checkAspect(SpellNode node, SpellPart part) {
        Optional<ResourceKey<IAspect>> resolved = part.aspect().resolve(node.aspect(), registries);
        if (resolved.isEmpty()) {
            return;
        }
        ResourceKey<IAspect> aspect = resolved.get();
        tally.countCrystal(aspect);
        if (!part.aspect().selectable()) {
            return;
        }
        Optional<ResourceKey<IAspect>> chosen = node.aspect();
        if (chosen.isPresent() && !chosen.get().equals(aspect)) {
            log.blockerAt(node, "aspect_not_allowed", SpellText.partName(node.part()), SpellText.aspectName(chosen.get()));
        }
        if (player != null) {
            checkAspectAccess(node, aspect);
        }
    }

    private void checkAspectAccess(SpellNode node, ResourceKey<IAspect> aspect) {
        Optional<Holder.Reference<IAspect>> holder = registries.lookup(IAspect.REGISTRY_KEY).flatMap(lookup -> lookup.get(aspect));
        if (holder.isPresent() && !AspectPoolAccess.isDiscovered(player, holder.get())) {
            log.blockerAt(node, "aspect_unknown", SpellText.aspectName(aspect));
        }
        Optional<AspectAffinity> affinity = Spells.affinity(registries, aspect);
        if (affinity.isPresent() && affinity.get().research().isPresent() && !ResearchGate.passes(player, affinity.get().research().get())) {
            log.blockerAt(node, "aspect_locked", SpellText.aspectName(aspect), SpellText.researchTitle(affinity.get().research().get().entry()));
        }
    }

    private void checkShape(SpellNode node, SpellPart part) {
        SpellBehavior behavior = part.behavior();
        int slots = behavior.maxChildren();
        int used = node.children().size();
        if (used > slots) {
            log.blockerAt(node, "too_many_children", SpellText.partName(node.part()), slots);
        }
        if (slots > 1) {
            tally.addBranch();
            if (used < behavior.minChildren()) {
                log.blockerAt(node, "empty_branch");
            }
        }
        tally.addRepeats(behavior.repeats(key -> settingFor(node, part, key)));
        if (used == 0 && !behavior.standalone()) {
            log.noticeAt(node, "leads_nowhere", SpellText.partName(node.part()));
        }
    }

    private static int settingFor(SpellNode node, SpellPart part, String key) {
        return part.setting(key).map(spec -> spec.clamp(node.settings().getOrDefault(key, spec.defaultValue()))).orElse(0);
    }
}
