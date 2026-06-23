package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksFMetals;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsFMetals;
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
        registerCubeAll(blockModels, TCBlocksFMetals.METAL_THAUMIUM_BLOCK.get(), "metal_thaumium");
        registerCubeAll(blockModels, TCBlocksFMetals.METAL_BRASS_BLOCK.get(), "metal_brass");
        registerCubeAll(blockModels, TCBlocksFMetals.METAL_VOID_BLOCK.get(), "metal_void");
        registerCubeAll(blockModels, TCBlocksFMetals.METAL_INFUSED_BLOCK.get(), "metal_infused");

        itemModels.generateFlatItem(TCItemsFMetals.INGOT_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.INGOT_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.INGOT_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.INGOT_INFUSED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.NUGGET_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.NUGGET_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.NUGGET_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_LEAD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_TIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItemsFMetals.CLUSTER_BRASS.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void registerCubeAll(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier textureId = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + textureName);
        Material texture = new Material(textureId);
        Identifier modelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(texture), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }
}
