package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import org.jspecify.annotations.Nullable;

public final class ArcaneLockChunkData {
    public static final MapCodec<ArcaneLockChunkData> CODEC = Entry.CODEC.listOf().optionalFieldOf("locks", List.of()).xmap(ArcaneLockChunkData::new, ArcaneLockChunkData::entries);

    private final Map<BlockPos, Lock> locks = new ConcurrentHashMap<>();

    public ArcaneLockChunkData() {}

    private ArcaneLockChunkData(List<Entry> entries) {
        for (Entry entry : entries) {
            locks.put(entry.pos(), new Lock(entry.owner(), playerSet(entry.ironAccess()), playerSet(entry.goldAccess())));
        }
    }

    public @Nullable UUID owner(BlockPos pos) {
        Lock lock = locks.get(pos);
        return lock == null ? null : lock.owner();
    }

    public void put(BlockPos pos, UUID owner) {
        locks.put(pos.immutable(), new Lock(owner, playerSet(List.of()), playerSet(List.of())));
    }

    public boolean remove(BlockPos pos) {
        return locks.remove(pos) != null;
    }

    public boolean canAccess(BlockPos pos, UUID player) {
        Lock lock = locks.get(pos);
        return lock != null && (lock.owner().equals(player) || lock.iron().contains(player) || lock.gold().contains(player));
    }

    public Set<UUID> accessors(BlockPos pos) {
        Lock lock = locks.get(pos);
        if (lock == null) {
            return Set.of();
        }
        Set<UUID> accessors = new HashSet<>(lock.iron());
        accessors.addAll(lock.gold());
        accessors.add(lock.owner());
        return accessors;
    }

    public boolean canDelegateIron(BlockPos pos, UUID player) {
        Lock lock = locks.get(pos);
        return lock != null && (lock.owner().equals(player) || lock.gold().contains(player));
    }

    public boolean grantAccess(BlockPos pos, UUID player, boolean gold) {
        Lock lock = locks.get(pos);
        return lock != null && (gold ? lock.gold() : lock.iron()).add(player);
    }

    private List<Entry> entries() {
        return locks.entrySet().stream().map(entry -> new Entry(entry.getKey(), entry.getValue().owner(), List.copyOf(entry.getValue().iron()), List.copyOf(entry.getValue().gold()))).toList();
    }

    private static Set<UUID> playerSet(List<UUID> players) {
        Set<UUID> set = ConcurrentHashMap.newKeySet();
        set.addAll(players);
        return set;
    }

    private record Lock(UUID owner, Set<UUID> iron, Set<UUID> gold) {
    }

    private record Entry(BlockPos pos, UUID owner, List<UUID> ironAccess, List<UUID> goldAccess) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner), UUIDUtil.CODEC.listOf().optionalFieldOf("iron_access", List.of()).forGetter(Entry::ironAccess),
                UUIDUtil.CODEC.listOf().optionalFieldOf("gold_access", List.of()).forGetter(Entry::goldAccess)).apply(inst, Entry::new));
    }
}
