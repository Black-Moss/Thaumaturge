package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class OreScanColors {
    public static final int DEFAULT = 0xC4C4C4;

    private static final List<Entry> TABLE = List.of(new Entry(Tags.Blocks.ORES_IRON, 0xD8B49C), new Entry(Tags.Blocks.ORES_COAL, 0x1C1A1E), new Entry(Tags.Blocks.ORES_REDSTONE, 0xF0140E),
            new Entry(Tags.Blocks.ORES_GOLD, 0xFFD23A), new Entry(Tags.Blocks.ORES_LAPIS, 0x1E3CC0), new Entry(Tags.Blocks.ORES_DIAMOND, 0x8CF2F4), new Entry(Tags.Blocks.ORES_EMERALD, 0x22DC5A),
            new Entry(Tags.Blocks.ORES_QUARTZ, 0xF2E8DA), new Entry(Tags.Blocks.ORES_COPPER, 0xE89A62), new Entry(TTBlockTags.ORES_AMBER, 0xF0A01E), new Entry(TTBlockTags.ORES_CINNABAR, 0x8A0E14));

    private OreScanColors() {}

    public static int of(BlockState state) {
        for (Entry entry : TABLE) {
            if (state.is(entry.ore())) {
                return entry.color();
            }
        }
        return DEFAULT;
    }

    private record Entry(TagKey<Block> ore, int color) {
    }
}
