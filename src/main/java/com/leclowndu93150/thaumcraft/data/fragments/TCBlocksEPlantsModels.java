package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksEPlants;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsEPlants;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;

public final class TCBlocksEPlantsModels {
    private TCBlocksEPlantsModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        cross(blockModels, TCBlocksEPlants.PLANT_SHIMMERLEAF.get());
        cross(blockModels, TCBlocksEPlants.PLANT_CINDERPEARL.get());
        cross(blockModels, TCBlocksEPlants.PLANT_VISHROOM.get());

        itemModels.generateFlatItem(TCItemsEPlants.PLANT_SHIMMERLEAF.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsEPlants.PLANT_CINDERPEARL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsEPlants.PLANT_VISHROOM.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void cross(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.CROSS.create(block, TextureMapping.cross(block), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }
}
