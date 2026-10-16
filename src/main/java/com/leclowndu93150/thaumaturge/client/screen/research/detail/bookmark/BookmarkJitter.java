package com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public final class BookmarkJitter {
    private static final int VALUES = 3;
    private static final int STAGE_SEED_FACTOR = 50;

    private final RandomSource random;

    private BookmarkJitter(long seed) {
        this.random = new LegacyRandomSource(seed);
    }

    public static BookmarkJitter forEntry(Identifier entryId, int displayedStage) {
        return new BookmarkJitter(entryId.toString().hashCode() + displayedStage * STAGE_SEED_FACTOR);
    }

    public int shift() {
        return random.nextInt(VALUES);
    }

    public int lean() {
        return random.nextInt(VALUES);
    }
}
