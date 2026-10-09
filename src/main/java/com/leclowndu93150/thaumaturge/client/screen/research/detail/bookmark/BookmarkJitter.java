package com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark;

import net.minecraft.resources.Identifier;

public final class BookmarkJitter {
    private static final int VALUES = 3;
    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;
    private static final long MIX_FIRST = 0xBF58476D1CE4E5B9L;
    private static final long MIX_SECOND = 0x94D049BB133111EBL;
    private static final int MIX_FIRST_SHIFT = 30;
    private static final int MIX_SECOND_SHIFT = 27;
    private static final int MIX_FINAL_SHIFT = 31;
    private static final long SHIFT_CHANNEL = 1L;
    private static final long LEAN_CHANNEL = 2L;
    private static final long CHANNEL_BITS = 2L;

    private final long key;

    private BookmarkJitter(long key) {
        this.key = key;
    }

    public static BookmarkJitter forEntry(Identifier entryId, int displayedStage) {
        return new BookmarkJitter(bookmarkKey(entryId, displayedStage));
    }

    public static long bookmarkKey(Identifier entryId, int displayedStage) {
        return mix(entryId.hashCode()) + displayedStage * GOLDEN_GAMMA;
    }

    public int shift(int ordinal) {
        return pick(ordinal, SHIFT_CHANNEL);
    }

    public int lean(int ordinal) {
        return pick(ordinal, LEAN_CHANNEL);
    }

    private int pick(int ordinal, long channel) {
        return (int) Math.floorMod(mix(key + ((long) ordinal << CHANNEL_BITS | channel)), (long) VALUES);
    }

    private static long mix(long value) {
        long mixed = value + GOLDEN_GAMMA;
        mixed = (mixed ^ (mixed >>> MIX_FIRST_SHIFT)) * MIX_FIRST;
        mixed = (mixed ^ (mixed >>> MIX_SECOND_SHIFT)) * MIX_SECOND;
        return mixed ^ (mixed >>> MIX_FINAL_SHIFT);
    }
}
