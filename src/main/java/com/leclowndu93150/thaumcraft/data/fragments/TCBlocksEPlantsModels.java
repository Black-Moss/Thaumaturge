package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class TCBlocksEPlantsModels {
    private TCBlocksEPlantsModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        cross(blockModels, TCBlocks.PLANT_SHIMMERLEAF.get());
        cross(blockModels, TCBlocks.PLANT_CINDERPEARL.get());
        cross(blockModels, TCBlocks.PLANT_VISHROOM.get());

        flatItemFromBlock(itemModels, TCItems.PLANT_SHIMMERLEAF.get(), TCBlocks.PLANT_SHIMMERLEAF.get());
        flatItemFromBlock(itemModels, TCItems.PLANT_CINDERPEARL.get(), TCBlocks.PLANT_CINDERPEARL.get());
        flatItemFromBlock(itemModels, TCItems.PLANT_VISHROOM.get(), TCBlocks.PLANT_VISHROOM.get());

        Identifier grassModel = Identifier.withDefaultNamespace("block/grass_block");
        MultiVariant grassVariant = new MultiVariant(WeightedList.of(new Variant(grassModel)));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TCBlocks.GRASS_AMBIENT.get(), grassVariant));
        itemModels.itemModelOutput.accept(TCItems.GRASS_AMBIENT.get(),
                ItemModelUtils.tintedModel(grassModel, new GrassColorSource(0.5F, 1.0F)));
    }

    static void flatItemFromBlock(ItemModelGenerators itemModels, Item item, Block block) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item),
                TextureMapping.layer0(TextureMapping.getBlockTexture(block)),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static void cross(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.CROSS.create(block, TextureMapping.cross(block), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }
}
