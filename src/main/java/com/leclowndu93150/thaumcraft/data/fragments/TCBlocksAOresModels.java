package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;

public final class TCBlocksAOresModels {
    private TCBlocksAOresModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(TCBlocks.ORE_AMBER.get());
        blockModels.createTrivialCube(TCBlocks.ORE_CINNABAR.get());
        blockModels.createTrivialCube(TCBlocks.ORE_QUARTZ.get());

        for (DyeColor dye : DyeColor.values()) {
            registerNitor(blockModels, itemModels, dye);
        }

        itemModels.generateFlatItem(TCItems.AMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CINNABAR.get(), ModelTemplates.FLAT_ITEM);
    }

    private static void registerNitor(BlockModelGenerators blockModels, ItemModelGenerators itemModels, DyeColor dye) {
        var block = TCBlocks.NITORS.get(dye).get();
        Identifier empty = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/empty");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(empty)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));

        var item = TCItems.NITORS.get(dye).get();
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/nitor_" + dye.getName());
        Material baseTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/nitor"));
        Material coreTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/nitor_core"));
        TextureMapping textures = TextureMapping.layered(baseTex, coreTex);
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, textures, itemModels.modelOutput);
        int rgb = dye.getTextureDiffuseColor() & 0xFFFFFF;
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(itemModelId, new Constant(rgb)));
    }
}
