package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

public final class SoundingScan {
    private static final int BASE_RANGE = 4;
    private static final int RANGE_PER_LEVEL = 4;
    private static final int TICKS_PER_BLOCK = 3;
    private static final int SECTION_MASK = 15;
    private static final int ADJACENT_REACH = 1;
    private static final Predicate<BlockState> ORE_TEST = state -> state.is(Tags.Blocks.ORES);
    private static final List<Vec3i> ADJACENT = adjacentOffsets();

    private SoundingScan() {}

    public static void perform(ServerLevel level, ServerPlayer viewer, BlockPos origin, int enchantLevel) {
        int range = BASE_RANGE + RANGE_PER_LEVEL * enchantLevel;
        ScanBox box = ScanBox.around(origin, range, level.getMinY(), level.getMaxY());
        Long2ObjectMap<BlockState> ores = new Long2ObjectOpenHashMap<>();
        walkChunks(level, box, ores);
        emit(viewer, group(ores), Vec3.atCenterOf(origin));
    }

    private static List<Vec3i> adjacentOffsets() {
        return BlockPos.betweenClosedStream(-ADJACENT_REACH, -ADJACENT_REACH, -ADJACENT_REACH, ADJACENT_REACH, ADJACENT_REACH, ADJACENT_REACH).<Vec3i>map(BlockPos::immutable)
                .sorted(Comparator.comparingInt(Vec3i::getX).thenComparingInt(Vec3i::getY).thenComparingInt(Vec3i::getZ)).toList();
    }

    private static void walkChunks(ServerLevel level, ScanBox box, Long2ObjectMap<BlockState> sink) {
        int lastChunkX = SectionPos.blockToSectionCoord(box.maxX());
        int lastChunkZ = SectionPos.blockToSectionCoord(box.maxZ());
        int firstSectionY = SectionPos.blockToSectionCoord(box.minY());
        int lastSectionY = SectionPos.blockToSectionCoord(box.maxY());
        for (int chunkX = SectionPos.blockToSectionCoord(box.minX()); chunkX <= lastChunkX; chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(box.minZ()); chunkZ <= lastChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk != null) {
                    for (int sectionY = firstSectionY; sectionY <= lastSectionY; sectionY++) {
                        LevelChunkSection section = chunk.getSection(level.getSectionIndexFromSectionY(sectionY));
                        if (section.maybeHas(ORE_TEST)) {
                            collectFrom(section, SectionPos.of(chunkX, sectionY, chunkZ), box, sink);
                        }
                    }
                }
            }
        }
    }

    private static void collectFrom(LevelChunkSection section, SectionPos sectionPos, ScanBox box, Long2ObjectMap<BlockState> sink) {
        int endX = Math.min(box.maxX(), sectionPos.maxBlockX());
        int endY = Math.min(box.maxY(), sectionPos.maxBlockY());
        int endZ = Math.min(box.maxZ(), sectionPos.maxBlockZ());
        for (int x = Math.max(box.minX(), sectionPos.minBlockX()); x <= endX; x++) {
            for (int y = Math.max(box.minY(), sectionPos.minBlockY()); y <= endY; y++) {
                for (int z = Math.max(box.minZ(), sectionPos.minBlockZ()); z <= endZ; z++) {
                    BlockState found = section.getBlockState(x & SECTION_MASK, y & SECTION_MASK, z & SECTION_MASK);
                    if (ORE_TEST.test(found)) {
                        sink.put(BlockPos.asLong(x, y, z), found);
                    }
                }
            }
        }
    }

    private static List<OreGroup> group(Long2ObjectMap<BlockState> ores) {
        List<OreGroup> groups = new ArrayList<>();
        while (!ores.isEmpty()) {
            long seed = ores.keySet().longIterator().nextLong();
            groups.add(spread(ores, seed));
        }
        return groups;
    }

    private static OreGroup spread(Long2ObjectMap<BlockState> ores, long seed) {
        BlockState state = ores.remove(seed);
        LongList members = new LongArrayList();
        LongArrayFIFOQueue frontier = new LongArrayFIFOQueue();
        members.add(seed);
        frontier.enqueue(seed);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        while (!frontier.isEmpty()) {
            BlockPos hub = BlockPos.of(frontier.dequeueLong());
            for (Vec3i offset : ADJACENT) {
                long neighbour = cursor.setWithOffset(hub, offset).asLong();
                if (ores.remove(neighbour, state)) {
                    members.add(neighbour);
                    frontier.enqueue(neighbour);
                }
            }
        }
        return new OreGroup(state, members);
    }

    private static void emit(ServerPlayer viewer, List<OreGroup> groups, Vec3 source) {
        for (OreGroup group : groups) {
            Vec3 centroid = group.centroid();
            int delay = (int) (centroid.distanceTo(source) * TICKS_PER_BLOCK);
            Effects.scanGlyph(viewer, centroid.x, centroid.y, centroid.z, OreScanColors.of(group.state()), delay);
        }
    }

    private record ScanBox(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        static ScanBox around(BlockPos origin, int range, int floorY, int ceilingY) {
            return new ScanBox(origin.getX() - range, origin.getX() + range, Math.max(origin.getY() - range, floorY), Math.min(origin.getY() + range, ceilingY), origin.getZ() - range,
                    origin.getZ() + range);
        }
    }

    private record OreGroup(BlockState state, LongList positions) {
        Vec3 centroid() {
            Vec3 total = Vec3.ZERO;
            LongIterator iterator = positions.iterator();
            while (iterator.hasNext()) {
                total = total.add(Vec3.atCenterOf(BlockPos.of(iterator.nextLong())));
            }
            double count = positions.size();
            return new Vec3(total.x / count, total.y / count, total.z / count);
        }
    }
}
