package com.leclowndu93150.thaumcraft.registry.blocks;

import com.leclowndu93150.thaumcraft.content.decor.BlockStairsTC;
import com.leclowndu93150.thaumcraft.content.decor.BlockStonePorous;
import com.leclowndu93150.thaumcraft.content.decor.BlockStoneTC;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TCBlocksCStone {
    public static final DeferredBlock<BlockStoneTC> STONE_ARCANE = TCBlocks.BLOCKS.registerBlock(
            "stone_arcane",
            props -> new BlockStoneTC(props, false),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ARCANE_BRICK = TCBlocks.BLOCKS.registerBlock(
            "stone_arcane_brick",
            props -> new BlockStoneTC(props, false),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT = TCBlocks.BLOCKS.registerBlock(
            "stone_ancient",
            props -> new BlockStoneTC(props, false),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_TILE = TCBlocks.BLOCKS.registerBlock(
            "stone_ancient_tile",
            props -> new BlockStoneTC(props, false),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_ROCK = TCBlocks.BLOCKS.registerBlock(
            "stone_ancient_rock",
            props -> new BlockStoneTC(props, true),
            TCBlocksCStone::unbreakableProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_GLYPHED = TCBlocks.BLOCKS.registerBlock(
            "stone_ancient_glyphed",
            props -> new BlockStoneTC(props, false),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_DOORWAY = TCBlocks.BLOCKS.registerBlock(
            "stone_ancient_doorway",
            props -> new BlockStoneTC(props, true),
            TCBlocksCStone::unbreakableProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ELDRITCH_TILE = TCBlocks.BLOCKS.registerBlock(
            "stone_eldritch_tile",
            props -> new BlockStoneTC(props, false),
            TCBlocksCStone::eldritchTileProps
    );

    public static final DeferredBlock<BlockStonePorous> STONE_POROUS = TCBlocks.BLOCKS.registerBlock(
            "stone_porous",
            BlockStonePorous::new,
            TCBlocksCStone::porousProps
    );

    public static final DeferredBlock<BlockStairsTC> STAIRS_ARCANE = TCBlocks.BLOCKS.registerBlock(
            "stairs_arcane",
            props -> new BlockStairsTC(STONE_ARCANE.get().defaultBlockState(), props),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStairsTC> STAIRS_ARCANE_BRICK = TCBlocks.BLOCKS.registerBlock(
            "stairs_arcane_brick",
            props -> new BlockStairsTC(STONE_ARCANE_BRICK.get().defaultBlockState(), props),
            TCBlocksCStone::stoneProps
    );

    public static final DeferredBlock<BlockStairsTC> STAIRS_ANCIENT = TCBlocks.BLOCKS.registerBlock(
            "stairs_ancient",
            props -> new BlockStairsTC(STONE_ANCIENT.get().defaultBlockState(), props),
            TCBlocksCStone::stoneProps
    );

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties unbreakableProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchTileProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 1000.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties porousProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private TCBlocksCStone() {}

    public static void touch() {}
}
