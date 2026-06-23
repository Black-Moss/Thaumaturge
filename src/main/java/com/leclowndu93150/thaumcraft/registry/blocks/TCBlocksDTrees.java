package com.leclowndu93150.thaumcraft.registry.blocks;

import com.leclowndu93150.thaumcraft.content.world.tree.BlockLeavesTC;
import com.leclowndu93150.thaumcraft.content.world.tree.BlockSaplingTC;
import com.leclowndu93150.thaumcraft.content.world.tree.TCTreeGrowers;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TCBlocksDTrees {
    public static final DeferredBlock<BlockSaplingTC> SAPLING_GREATWOOD = TCBlocks.BLOCKS.registerBlock(
            "sapling_greatwood",
            props -> new BlockSaplingTC(TCTreeGrowers.GREATWOOD, props),
            props -> props
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final DeferredBlock<BlockSaplingTC> SAPLING_SILVERWOOD = TCBlocks.BLOCKS.registerBlock(
            "sapling_silverwood",
            props -> new BlockSaplingTC(TCTreeGrowers.SILVERWOOD, props),
            props -> props
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final DeferredBlock<RotatedPillarBlock> LOG_GREATWOOD = TCBlocks.BLOCKS.registerBlock(
            "log_greatwood",
            RotatedPillarBlock::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    public static final DeferredBlock<RotatedPillarBlock> LOG_SILVERWOOD = TCBlocks.BLOCKS.registerBlock(
            "log_silverwood",
            RotatedPillarBlock::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava()
    );

    public static final DeferredBlock<BlockLeavesTC> LEAVES_GREATWOOD = TCBlocks.BLOCKS.registerBlock(
            "leaves_greatwood",
            props -> new BlockLeavesTC(0.01F, props),
            TCBlocksDTrees::leavesProps
    );

    public static final DeferredBlock<BlockLeavesTC> LEAVES_SILVERWOOD = TCBlocks.BLOCKS.registerBlock(
            "leaves_silverwood",
            props -> new BlockLeavesTC(0.01F, props),
            props -> leavesProps(props).mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> PLANK_GREATWOOD = TCBlocks.BLOCKS.registerBlock(
            "plank_greatwood",
            Block::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    public static final DeferredBlock<Block> PLANK_SILVERWOOD = TCBlocks.BLOCKS.registerBlock(
            "plank_silverwood",
            Block::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    private static net.minecraft.world.level.block.state.BlockBehaviour.Properties leavesProps(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties props
    ) {
        return props
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    private TCBlocksDTrees() {}

    public static void touch() {
        TCTreeGrowers.touch();
    }
}
