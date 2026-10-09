package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutResult;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.RoomPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.CompiledRoom;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

final class MazeAssembler {
    private static final int HALL_HEADROOM = 14;
    private static final int MAX_TRIGGERS = 256;

    private MazeAssembler() {}

    static Assembly assemble(MazeId id, GlobalPos origin, MazeGeometry geometry, long seed, ResourceKey<LabyrinthDefinition> definition, LabyrinthTuning tuning, LayoutResult layout, Function<Identifier, Optional<CompiledRoom>> rooms) {
        Palette palette = new Palette();
        Harvest harvest = new Harvest();
        long[] packedRooms = layout.rooms().stream().mapToLong(placement -> {
            long packed = palette.pack(placement);
            rooms.apply(placement.template()).ifPresent(compiled -> harvest.collect(placement, compiled, geometry, tuning));
            return packed;
        }).toArray();
        harvest.points.putIfAbsent(LabyrinthLandmarks.ARRIVAL, geometry.cellCenter(layout.cellX(layout.portalCell()), layout.cellZ(layout.portalCell())));
        MazeLandmarks landmarks = new MazeLandmarks(Map.copyOf(harvest.points), List.copyOf(harvest.barriers), List.copyOf(harvest.glyphs), hallBounds(geometry, layout));
        Optional<ResourceKey<LabyrinthEncounter>> encounter = layout.encounter().flatMap(Holder::unwrapKey);
        MazePlan plan = new MazePlan(id, origin, geometry, seed, definition, encounter, tuning, List.copyOf(palette.rooms), List.copyOf(palette.templates), packedRooms, packCells(layout), landmarks);
        return new Assembly(plan, List.copyOf(harvest.triggers));
    }

    private static int[] packCells(LayoutResult layout) {
        return IntStream.range(0, layout.edges().length).map(cell -> {
            int[] towards = new int[MazeCells.TARGETS];
            Arrays.setAll(towards, target -> layout.directions()[target][cell]);
            return MazeCells.packCell(layout.edges()[cell], layout.roomOfCell()[cell], towards);
        }).toArray();
    }

    private static BoundingBox hallBounds(MazeGeometry geometry, LayoutResult layout) {
        int hallCell = layout.hallCell();
        int hallRoom = hallCell < 0 ? -1 : layout.roomOfCell()[hallCell];
        if (hallRoom < 0) {
            return new BoundingBox(geometry.cellCenter(0, 0));
        }
        RoomPlacement hall = layout.rooms().get(hallRoom);
        BlockPos near = corner(geometry, hall, 0, 0);
        BlockPos far = corner(geometry, hall, hall.width(), hall.depth());
        int floor = geometry.floorY();
        return new BoundingBox(near.getX(), floor + 1, near.getZ(), far.getX() - 1, floor + HALL_HEADROOM, far.getZ() - 1);
    }

    private static BlockPos corner(MazeGeometry geometry, RoomPlacement placement, int extentX, int extentZ) {
        return geometry.cellMin(placement.anchorX() + extentX, placement.anchorZ() + extentZ);
    }

    record Assembly(MazePlan plan, List<PendingTrigger> triggers) {
    }

    private static final class Palette {
        private final List<ResourceKey<RoomType>> rooms = new ArrayList<>();
        private final List<Identifier> templates = new ArrayList<>();

        long pack(RoomPlacement placement) {
            int roomSlot = slotOf(rooms, placement.room().unwrapKey().orElseThrow());
            int templateSlot = slotOf(templates, placement.template());
            return MazeCells.packRoom(roomSlot, templateSlot, placement.transform(), placement.anchorX(), placement.anchorZ());
        }

        private static <T> int slotOf(List<T> entries, T entry) {
            int existing = entries.indexOf(entry);
            if (existing >= 0) {
                return existing;
            }
            entries.add(entry);
            return entries.size() - 1;
        }
    }

    private static final class Harvest {
        private final Map<Identifier, BlockPos> points = new LinkedHashMap<>();
        private final List<BlockPos> barriers = new ArrayList<>();
        private final List<BlockPos> glyphs = new ArrayList<>();
        private final List<PendingTrigger> triggers = new ArrayList<>();

        void collect(RoomPlacement placement, CompiledRoom compiled, MazeGeometry geometry, LabyrinthTuning tuning) {
            int transform = placement.transform();
            BlockPos roomOrigin = RoomTransform.origin(geometry.cellMin(placement.anchorX(), placement.anchorZ()), transform, compiled.size());
            for (CompiledRoom.Marker marker : compiled.markers()) {
                accept(marker.marker(), RoomTransform.toWorld(marker.local(), transform, roomOrigin), transform, tuning);
            }
            for (BlockPos local : compiled.barriers()) {
                barriers.add(RoomTransform.toWorld(local, transform, roomOrigin));
            }
        }

        private void accept(LabyrinthMarker marker, BlockPos world, int transform, LabyrinthTuning tuning) {
            if (tuning.disables(marker)) {
                return;
            }
            marker.landmark().ifPresent(landmark -> points.putIfAbsent(landmark, world));
            if (marker.wayfinding()) {
                glyphs.add(world);
            }
            if (marker.phase() == MarkerPhase.TRIGGER && triggers.size() < MAX_TRIGGERS) {
                triggers.add(new PendingTrigger(triggers.size(), world, marker.triggerRadius(), transform, marker));
            }
        }
    }
}
