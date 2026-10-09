package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.core.Holder;

public final class NoteRules {
    private NoteRules() {}

    public record Completion(boolean complete, List<ResearchNoteData.Cell> prunedCells) {
    }

    public static boolean connects(Holder<IAspect> first, Holder<IAspect> second, Predicate<Holder<IAspect>> discovered) {
        if (first == null || second == null) {
            return false;
        }
        return !sameKey(first, second) && discovered.test(first) && discovered.test(second) && (composedOf(first, second) || composedOf(second, first));
    }

    public static Completion checkCompletion(ResearchNoteData note, Predicate<Holder<IAspect>> discovered) {
        List<ResearchNoteData.Cell> all = note.cells();
        List<ResearchNoteData.Cell> anchors = all.stream().filter(NoteRules::isRoot).toList();
        if (anchors.isEmpty()) {
            return new Completion(false, all);
        }
        Set<HexGrid.Hex> linked = flood(note.cellMap(), anchors.get(0).hex(), discovered);
        if (!anchors.stream().allMatch(anchor -> linked.contains(anchor.hex()))) {
            return new Completion(false, all);
        }
        return new Completion(true, all.stream().filter(cell -> isRoot(cell) || linked.contains(cell.hex())).toList());
    }

    private static boolean isRoot(ResearchNoteData.Cell cell) {
        return cell.type() == ResearchNoteData.TYPE_ROOT;
    }

    private static Set<HexGrid.Hex> flood(Map<HexGrid.Hex, ResearchNoteData.Cell> cells, HexGrid.Hex start, Predicate<Holder<IAspect>> discovered) {
        Set<HexGrid.Hex> seen = new HashSet<>(Set.of(start));
        Deque<HexGrid.Hex> frontier = new ArrayDeque<>(List.of(start));
        while (!frontier.isEmpty()) {
            HexGrid.Hex here = frontier.pop();
            ResearchNoteData.Cell origin = cells.get(here);
            if (origin == null || !origin.active()) {
                continue;
            }
            IntStream.range(0, HexGrid.NEIGHBOURS.length).mapToObj(here::neighbour).filter(there -> !seen.contains(there) && linksTo(origin, cells.get(there), discovered)).toList().forEach(there -> {
                seen.add(there);
                frontier.push(there);
            });
        }
        return seen;
    }

    private static boolean linksTo(ResearchNoteData.Cell origin, ResearchNoteData.Cell target, Predicate<Holder<IAspect>> discovered) {
        return target != null && target.active() && connects(origin.aspectOrNull(), target.aspectOrNull(), discovered);
    }

    private static boolean composedOf(Holder<IAspect> compound, Holder<IAspect> part) {
        return compound.value().components().stream().anyMatch(component -> sameKey(component, part));
    }

    private static boolean sameKey(Holder<IAspect> first, Holder<IAspect> second) {
        return first.unwrapKey().filter(key -> Optional.of(key).equals(second.unwrapKey())).isPresent();
    }
}
