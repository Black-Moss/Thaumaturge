package com.leclowndu93150.thaumcraft.registry.blocks;

import com.leclowndu93150.thaumcraft.content.world.plant.BlockPlantCinderpearl;
import com.leclowndu93150.thaumcraft.content.world.plant.BlockPlantShimmerleaf;
import com.leclowndu93150.thaumcraft.content.world.plant.BlockPlantVishroom;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class TCBlocksEPlants {
    public static final DeferredBlock<BlockPlantShimmerleaf> PLANT_SHIMMERLEAF = TCBlocks.BLOCKS.registerBlock(
            "shimmerleaf",
            BlockPlantShimmerleaf::new,
            props -> props
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockPlantCinderpearl> PLANT_CINDERPEARL = TCBlocks.BLOCKS.registerBlock(
            "cinderpearl",
            BlockPlantCinderpearl::new,
            props -> props
                    .mapColor(MapColor.COLOR_ORANGE)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 8)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockPlantVishroom> PLANT_VISHROOM = TCBlocks.BLOCKS.registerBlock(
            "vishroom",
            BlockPlantVishroom::new,
            props -> props
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion()
    );

    private TCBlocksEPlants() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
