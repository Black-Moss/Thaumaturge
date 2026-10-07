package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.guardian.GuardianPosts;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.BlockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GlyphMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GuardianPostMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LootMarker;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class RoomKit {
    static final double MID = MazeGeometry.CELL / 2.0;

    private static final int COMMON_WEIGHT = 6;
    private static final int UNCOMMON_WEIGHT = 3;
    private static final int RARE_WEIGHT = 1;

    private RoomKit() {}

    static LootMarker urns(float chance) {
        return tiered(TTBlocks.LOOT_URN_COMMON.get(), TTBlocks.LOOT_URN_UNCOMMON.get(), TTBlocks.LOOT_URN_RARE.get(), chance);
    }

    static LootMarker crates(float chance) {
        return tiered(TTBlocks.LOOT_CRATE_COMMON.get(), TTBlocks.LOOT_CRATE_UNCOMMON.get(), TTBlocks.LOOT_CRATE_RARE.get(), chance);
    }

    private static LootMarker tiered(Block common, Block uncommon, Block rare, float chance) {
        return new LootMarker(WeightedList.of(List.of(new Weighted<>(common.defaultBlockState(), COMMON_WEIGHT), new Weighted<>(uncommon.defaultBlockState(), UNCOMMON_WEIGHT),
                new Weighted<>(rare.defaultBlockState(), RARE_WEIGHT))), chance);
    }

    static BlockMarker embedded(BlockState state, float chance, BlockState otherwise) {
        return new BlockMarker(WeightedList.of(state), chance, true, Optional.empty(), Optional.of(otherwise));
    }

    static BlockMarker scatter(BlockState state, float chance) {
        return new BlockMarker(WeightedList.of(state), chance, true, Optional.empty(), Optional.empty());
    }

    static GuardianPostMarker keyWard() {
        return new GuardianPostMarker(Optional.empty(), Optional.of(GuardianPosts.KEY_ROOM_WARD));
    }

    static void glyph(RoomCanvas canvas, int x, int y, int z, Direction facing) {
        canvas.marker(x, y, z, new GlyphMarker(LabyrinthBlocks.glyph(), facing));
    }

    static void entrance(RoomCanvas canvas, int depth) {
        canvas.socket(new RoomSocket(0, 0, Direction.NORTH), depth);
    }

    static void pillar(RoomCanvas canvas, int x, int z, int y0, int y1, BlockState state) {
        canvas.fill(RoomShape.box(x, y0, z, x, y1, z), state);
    }

    static void pillars(RoomCanvas canvas, int[][] spots, int y0, int y1, BlockState state) {
        for (int[] spot : spots) {
            pillar(canvas, spot[0], spot[1], y0, y1, state);
        }
    }
}
