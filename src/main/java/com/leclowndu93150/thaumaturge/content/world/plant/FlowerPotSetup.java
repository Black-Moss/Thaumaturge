package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class FlowerPotSetup {
    private FlowerPotSetup() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FlowerPotBlock flowerPot = (FlowerPotBlock) Blocks.FLOWER_POT;
            flowerPot.addPlant(TCBlocks.SAPLING_GREATWOOD.getId(), TCBlocks.POTTED_SAPLING_GREATWOOD);
            flowerPot.addPlant(TCBlocks.SAPLING_SILVERWOOD.getId(), TCBlocks.POTTED_SAPLING_SILVERWOOD);
            flowerPot.addPlant(TCBlocks.PLANT_SHIMMERLEAF.getId(), TCBlocks.POTTED_SHIMMERLEAF);
            flowerPot.addPlant(TCBlocks.PLANT_CINDERPEARL.getId(), TCBlocks.POTTED_CINDERPEARL);
            flowerPot.addPlant(TCBlocks.PLANT_VISHROOM.getId(), TCBlocks.POTTED_VISHROOM);
        });
    }
}
