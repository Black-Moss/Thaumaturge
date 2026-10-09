package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class SpellAnalyzer {
    public static final float VIS_PER_COMPLEXITY = 0.2F;
    private static final float COOLDOWN_BASE = 5.0F;
    private static final float COOLDOWN_EXPONENT = 1.5F;
    private static final float COOLDOWN_DIVISOR = 6.0F;
    private static final int CHARGE_BASE = 10;
    private static final int CHARGE_DIVISOR = 2;
    private static final int PULSE_BASE = 4;
    private static final int PULSE_DIVISOR = 8;
    private static final int PULSE_MAX = 12;

    private SpellAnalyzer() {}

    public static SpellSummary analyze(Spell spell, @Nullable FocusTier tier, HolderLookup.Provider registries, @Nullable Player player) {
        SpellProblemLog log = new SpellProblemLog();
        SpellTally tally = new SpellTally();
        SpellNode root = spell.root();
        if (!root.part().equals(Spell.ORIGIN)) {
            log.blockerAt(root, "bad_root");
        }
        if (spell.isEmpty()) {
            log.blocker("empty");
        }
        new NodeAuditor(registries, player, log, tally).auditBelow(root);
        int total = Math.round(tally.complexity());
        int depth = root.depth() - 1;
        if (!spell.isEmpty() && !tally.effectSeen()) {
            log.notice("no_effect");
        }
        int budget = 0;
        if (tier != null) {
            budget = tier.complexity();
            TierLimit.Usage usage = new TierLimit.Usage(total, depth, tally.branches(), tally.repeats());
            for (TierLimit limit : TierLimit.values()) {
                limit.enforce(usage, tier, log);
            }
        }
        float vis = (total * VIS_PER_COMPLEXITY + tally.extraVis()) * tally.visMultiplier();
        int xp = Math.max(1, Math.round(Mth.sqrt(total)));
        int cooldown = cooldownTicks(total, tally.cooldownMultiplier());
        int charge = CHARGE_BASE + total / CHARGE_DIVISOR;
        int pulse = Mth.clamp(PULSE_BASE + total / PULSE_DIVISOR, PULSE_BASE, PULSE_MAX);
        return new SpellSummary(total, budget, vis, xp, tally.crystals(), depth, tally.branches(), tally.repeats(), cooldown, charge, pulse, log.blockersFirst());
    }

    private static int cooldownTicks(int total, float multiplier) {
        float base = COOLDOWN_BASE + (float) Math.pow(total, COOLDOWN_EXPONENT) / COOLDOWN_DIVISOR;
        return Math.round(base * multiplier);
    }

    public static float nodeComplexity(SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        float sum = part.complexity();
        for (SettingSpec spec : part.settings()) {
            sum += stepsOf(node, spec) * spec.complexity();
        }
        Optional<AspectAffinity> affinity = chosenAffinity(node, part, registries);
        return sum + affinity.map(AspectAffinity::complexity).orElse(0);
    }

    public static float nodeVis(SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        float sum = part.vis();
        for (SettingSpec spec : part.settings()) {
            sum += stepsOf(node, spec) * spec.vis();
        }
        Optional<AspectAffinity> affinity = chosenAffinity(node, part, registries);
        return sum + affinity.map(AspectAffinity::vis).orElse(0.0F);
    }

    private static int stepsOf(SpellNode node, SettingSpec spec) {
        return spec.steps(node.settings().getOrDefault(spec.key(), spec.defaultValue()));
    }

    private static Optional<AspectAffinity> chosenAffinity(SpellNode node, SpellPart part, HolderLookup.Provider registries) {
        if (!part.aspect().selectable()) {
            return Optional.empty();
        }
        return part.aspect().resolve(node.aspect(), registries).flatMap(aspect -> Spells.affinity(registries, aspect));
    }
}
