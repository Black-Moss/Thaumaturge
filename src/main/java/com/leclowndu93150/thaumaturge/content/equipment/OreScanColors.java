package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class OreScanColors {
    public static final int DEFAULT = 0xC0C0C0;

    private static final List<Entry> TABLE = List.of(new Entry(Tags.Blocks.ORES_IRON, 0xD8AF93), new Entry(Tags.Blocks.ORES_COAL, 0x101010), new Entry(Tags.Blocks.ORES_REDSTONE, 0xFF0000),
            new Entry(Tags.Blocks.ORES_GOLD, 0xFCEE4B), new Entry(Tags.Blocks.ORES_LAPIS, 0x1445BC), new Entry(Tags.Blocks.ORES_DIAMOND, 0x5DECF5), new Entry(Tags.Blocks.ORES_EMERALD, 0x17DD62),
            new Entry(Tags.Blocks.ORES_QUARTZ, 0xE5DED5), new Entry(Tags.Blocks.ORES_COPPER, 0xFD9C55), new Entry(TTBlockTags.ORES_AMBER, 0xFDB325), new Entry(TTBlockTags.ORES_CINNABAR, 0x9B0508));

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
