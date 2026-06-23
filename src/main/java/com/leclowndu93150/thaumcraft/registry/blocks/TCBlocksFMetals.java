package com.leclowndu93150.thaumcraft.registry.blocks;

import com.leclowndu93150.thaumcraft.content.metal.BlockMetalTC;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TCBlocksFMetals {
    public static final DeferredBlock<BlockMetalTC> METAL_THAUMIUM_BLOCK = TCBlocks.BLOCKS.registerBlock(
            "metal_thaumium",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockMetalTC> METAL_BRASS_BLOCK = TCBlocks.BLOCKS.registerBlock(
            "metal_brass",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockMetalTC> METAL_VOID_BLOCK = TCBlocks.BLOCKS.registerBlock(
            "metal_void",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockMetalTC> METAL_INFUSED_BLOCK = TCBlocks.BLOCKS.registerBlock(
            "metal_infused",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    private TCBlocksFMetals() {}

    public static void touch() {}
}
