package com.leclowndu93150.thaumcraft.client.aura;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class ClientAuraCache {
    private static final Map<Long, Snapshot> ENTRIES = new ConcurrentHashMap<>();
    private static final long STALE_TICKS = 60L;
    private static long currentTick;

    private ClientAuraCache() {}

    public static void put(ChunkPos pos, short base, float vis, float flux) {
        ENTRIES.put(ChunkPos.pack(pos.x(), pos.z()), new Snapshot(base, vis, flux, currentTick));
    }

    public static @Nullable Snapshot get(ChunkPos pos) {
        return ENTRIES.get(ChunkPos.pack(pos.x(), pos.z()));
    }

    public static boolean shouldRefresh(ChunkPos pos) {
        Snapshot snap = ENTRIES.get(ChunkPos.pack(pos.x(), pos.z()));
        return snap == null || currentTick - snap.tick() > STALE_TICKS;
    }

    public static void tick() {
        currentTick++;
    }

    public static void clear() {
        ENTRIES.clear();
        currentTick = 0L;
    }

    public record Snapshot(short base, float vis, float flux, long tick) {
    }
}
