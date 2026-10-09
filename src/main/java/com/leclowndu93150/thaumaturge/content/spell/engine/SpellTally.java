package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;

final class SpellTally {
    private final Map<ResourceKey<SpellPart>, Integer> seen = new HashMap<>();
    private final Map<ResourceKey<IAspect>, Integer> crystals = new LinkedHashMap<>();
    private float complexity;
    private float extraVis;
    private float visMultiplier = 1.0F;
    private float cooldownMultiplier = 1.0F;
    private int branches;
    private int repeats;
    private boolean effectSeen;

    int countUse(ResourceKey<SpellPart> part) {
        return seen.merge(part, 1, Integer::sum);
    }

    void countCrystal(ResourceKey<IAspect> aspect) {
        crystals.merge(aspect, 1, Integer::sum);
    }

    void addCost(float nodeComplexity, int uses, float nodeVis) {
        complexity += nodeComplexity * (uses + 1) / 2.0F;
        extraVis += nodeVis;
    }

    void scale(float vis, float cooldown) {
        visMultiplier *= vis;
        cooldownMultiplier *= cooldown;
    }

    void addBranch() {
        branches++;
    }

    void addRepeats(int amount) {
        repeats += amount;
    }

    void markEffect() {
        effectSeen = true;
    }

    float complexity() {
        return complexity;
    }

    float extraVis() {
        return extraVis;
    }

    float visMultiplier() {
        return visMultiplier;
    }

    float cooldownMultiplier() {
        return cooldownMultiplier;
    }

    int branches() {
        return branches;
    }

    int repeats() {
        return repeats;
    }

    boolean effectSeen() {
        return effectSeen;
    }

    Map<ResourceKey<IAspect>, Integer> crystals() {
        return crystals;
    }
}
