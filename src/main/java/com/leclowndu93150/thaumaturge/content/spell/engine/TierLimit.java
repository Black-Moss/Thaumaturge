package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import java.util.function.ToIntFunction;

enum TierLimit {
    COMPLEXITY("too_complex", Usage::complexity, FocusTier::complexity), DEPTH("too_deep", Usage::depth, FocusTier::depth), BRANCHES("too_many_branches", Usage::branches,
            FocusTier::branches), REPEATS("too_many_repeats", Usage::repeats, FocusTier::repeats);

    private final String problemKey;
    private final ToIntFunction<Usage> used;
    private final ToIntFunction<FocusTier> cap;

    TierLimit(String problemKey, ToIntFunction<Usage> used, ToIntFunction<FocusTier> cap) {
        this.problemKey = problemKey;
        this.used = used;
        this.cap = cap;
    }

    void enforce(Usage usage, FocusTier tier, SpellProblemLog log) {
        int value = used.applyAsInt(usage);
        int limit = cap.applyAsInt(tier);
        if (value > limit) {
            log.blocker(problemKey, value, limit);
        }
    }

    record Usage(int complexity, int depth, int branches, int repeats) {
    }
}
