package com.leclowndu93150.thaumaturge.content.research.note;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class NoteGenerator {
    private static final int MIN_COMPLEXITY = 1;
    private static final int MAX_COMPLEXITY = 3;
    private static final int RADIUS_BASE = 1;
    private static final int HOLES_PER_COMPLEXITY = 2;
    private static final int MAX_HOLE_ATTEMPTS = 100;
    private static final int MIN_ROOT_NEIGHBOURS = 2;
    private static final int HEX_DIRECTIONS = 6;
    private static final int DEFAULT_COLOR = 0x999999;
    private static final int NO_COPIES = 0;

    private NoteGenerator() {}

    public static ResearchNoteData generate(Identifier entry, int index, AspectList anchors, int complexity, RandomSource random) {
        int level = Mth.clamp(complexity, MIN_COMPLEXITY, MAX_COMPLEXITY);
        int radius = RADIUS_BASE + Math.min(MAX_COMPLEXITY, level);
        int color = primaryColor(anchors);
        Map<HexGrid.Hex, ResearchNoteData.Cell> grid = blankGrid(radius);
        placeRoots(grid, anchors, radius, random);
        punchHoles(grid, level > MIN_COMPLEXITY ? level * HOLES_PER_COMPLEXITY : 0, random);
        return new ResearchNoteData(entry, index, color, false, NO_COPIES, List.copyOf(grid.values()));
    }

    private static Map<HexGrid.Hex, ResearchNoteData.Cell> blankGrid(int radius) {
        Map<HexGrid.Hex, ResearchNoteData.Cell> grid = new LinkedHashMap<>();
        HexGrid.disk(radius).forEach(hex -> grid.put(hex, new ResearchNoteData.Cell(hex, ResearchNoteData.TYPE_BLANK, Optional.empty())));
        return grid;
    }

    public static int primaryColor(AspectList anchors) {
        return anchors.entries().stream().max(Comparator.comparingInt(AspectInstance::amount)).map(strongest -> strongest.aspect().value().color()).orElse(DEFAULT_COLOR);
    }

    private static void placeRoots(Map<HexGrid.Hex, ResearchNoteData.Cell> grid, AspectList anchors, int radius, RandomSource random) {
        List<AspectInstance> wanted = anchors.entries();
        int capacity = HexGrid.ring(radius).size();
        List<HexGrid.Hex> spots = HexGrid.distributeRing(radius, Math.min(capacity, wanted.size()), random);
        for (int slot = 0; slot < spots.size(); slot++) {
            grid.put(spots.get(slot), rootAt(spots.get(slot), wanted.get(slot)));
        }
    }

    private static ResearchNoteData.Cell rootAt(HexGrid.Hex spot, AspectInstance anchor) {
        return new ResearchNoteData.Cell(spot, ResearchNoteData.TYPE_ROOT, Optional.of(anchor.aspect()));
    }

    private static void punchHoles(Map<HexGrid.Hex, ResearchNoteData.Cell> grid, int holes, RandomSource random) {
        List<HexGrid.Hex> pool = grid.values().stream().filter(NoteGenerator::isBlank).map(ResearchNoteData.Cell::hex).collect(Collectors.toCollection(ArrayList::new));
        int removed = 0;
        for (int tries = MAX_HOLE_ATTEMPTS; tries > 0 && removed < holes && !pool.isEmpty(); tries--) {
            removed += carve(grid, pool.remove(random.nextInt(pool.size())));
        }
    }

    private static int carve(Map<HexGrid.Hex, ResearchNoteData.Cell> grid, HexGrid.Hex victim) {
        if (!anchorsSurvive(grid, victim)) {
            return 0;
        }
        grid.remove(victim);
        return 1;
    }

    private static boolean isBlank(ResearchNoteData.Cell cell) {
        return cell.type() == ResearchNoteData.TYPE_BLANK;
    }

    private static boolean anchorsSurvive(Map<HexGrid.Hex, ResearchNoteData.Cell> grid, HexGrid.Hex victim) {
        return IntStream.range(0, HEX_DIRECTIONS).mapToObj(victim::neighbour).noneMatch(next -> isRoot(grid.get(next)) && liveLinks(grid, next, victim) < MIN_ROOT_NEIGHBOURS);
    }

    private static boolean isRoot(ResearchNoteData.Cell cell) {
        return cell != null && cell.type() == ResearchNoteData.TYPE_ROOT;
    }

    private static long liveLinks(Map<HexGrid.Hex, ResearchNoteData.Cell> grid, HexGrid.Hex centre, HexGrid.Hex ignored) {
        return IntStream.range(0, HEX_DIRECTIONS).mapToObj(centre::neighbour).filter(next -> grid.containsKey(next) && !next.equals(ignored)).count();
    }
}
