package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class SpellProblemLog {
    private final List<SpellProblem> entries = new ArrayList<>();

    void blockerAt(SpellNode node, String key, Object... args) {
        entries.add(SpellProblem.at(node, SpellText.problem(key, args), true));
    }

    void noticeAt(SpellNode node, String key, Object... args) {
        entries.add(SpellProblem.at(node, SpellText.problem(key, args), false));
    }

    void blocker(String key, Object... args) {
        entries.add(SpellProblem.of(SpellText.problem(key, args), true));
    }

    void notice(String key, Object... args) {
        entries.add(SpellProblem.of(SpellText.problem(key, args), false));
    }

    List<SpellProblem> blockersFirst() {
        entries.sort(Comparator.comparing(problem -> !problem.fatal()));
        return entries;
    }
}
