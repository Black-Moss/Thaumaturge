package com.leclowndu93150.thaumcraft.registry.blocks;

import com.leclowndu93150.thaumcraft.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TCBlocksAOres {
    public static final DeferredBlock<Block> ORE_AMBER = TCBlocks.BLOCKS.registerBlock(
            "ore_amber",
            Block::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> ORE_CINNABAR = TCBlocks.BLOCKS.registerBlock(
            "ore_cinnabar",
            Block::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> ORE_QUARTZ = TCBlocks.BLOCKS.registerBlock(
            "ore_quartz",
            Block::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final Map<DyeColor, DeferredBlock<BlockNitor>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, TCBlocks.BLOCKS.registerBlock(
                    "nitor_" + dye.getName(),
                    props -> new BlockNitor(dye, props),
                    () -> nitorProps(dye)
            ));
        }
    }

    private static BlockBehaviour.Properties nitorProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .noCollision()
                .pushReaction(PushReaction.DESTROY);
    }

    private TCBlocksAOres() {}

    public static void touch() {}
}
