package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;

public final class TCBlocksFMetalsModels {
    private TCBlocksFMetalsModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerCubeAll(blockModels, TCBlocks.METAL_THAUMIUM_BLOCK.get(), "metal_thaumium");
        registerCubeAll(blockModels, TCBlocks.METAL_BRASS_BLOCK.get(), "metal_brass");
        registerCubeAll(blockModels, TCBlocks.METAL_VOID_BLOCK.get(), "metal_void");
        registerCubeAll(blockModels, TCBlocks.METAL_INFUSED_BLOCK.get(), "metal_infused");

        itemModels.generateFlatItem(TCItems.INGOT_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_INFUSED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_LEAD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_TIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_BRASS.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void registerCubeAll(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier textureId = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + textureName);
        Material texture = new Material(textureId);
        Identifier modelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(texture), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }
}
