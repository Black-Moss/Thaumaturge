package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;

public final class TCBlocksTaintModels {
    private TCBlocksTaintModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        simpleCube(blockModels, TCBlocks.TAINT_ROCK.get(), "taint_rock");
        simpleCube(blockModels, TCBlocks.TAINT_SOIL.get(), "taint_soil");
        simpleCube(blockModels, TCBlocks.TAINT_CRUST.get(), "taint_crust");

        registerFluxGoo(blockModels);
        registerTaintGeyser(blockModels);
        registerTaintLog(blockModels);
        registerTaintFeature(blockModels);
        registerTaintFibre(blockModels);

        itemModels.generateFlatItem(TCBlocks.TAINT_ROCK.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCBlocks.TAINT_SOIL.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCBlocks.TAINT_CRUST.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCBlocks.TAINT_GEYSER.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCBlocks.TAINT_LOG.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCBlocks.TAINT_FEATURE.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCBlocks.TAINT_FIBRE.asItem(), ModelTemplates.FLAT_ITEM);
    }

    private static void simpleCube(BlockModelGenerators blockModels, net.minecraft.world.level.block.Block block, String modelName) {
        MultiVariant variant = variantOf(modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private static void registerFluxGoo(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("flux_goo");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.FLUX_GOO.get(), variant));
    }

    private static void registerTaintGeyser(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("taint_geyser");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_GEYSER.get(), variant));
    }

    private static void registerTaintLog(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("taint_log");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_LOG.get(), variant));
    }

    private static void registerTaintFeature(BlockModelGenerators blockModels) {
        MultiVariant variant = variantOf("taint_feature");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.TAINT_FEATURE.get(), variant));
    }

    private static void registerTaintFibre(BlockModelGenerators blockModels) {
        MultiVariant fibre = variantOf("taint_fibre");
        MultiVariant growth1 = variantOf("taint_growth_1");
        MultiVariant growth2 = variantOf("taint_growth_2");
        MultiVariant growth3 = variantOf("taint_growth_3");
        MultiVariant growth4 = variantOf("taint_growth_4");
        MultiPartGenerator gen = MultiPartGenerator.multiPart(TCBlocks.TAINT_FIBRE.get());
        gen.with(new ConditionBuilder().term(BlockTaintFibre.NORTH, true), fibre);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.EAST, true), fibre.with(BlockModelGenerators.Y_ROT_90));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.SOUTH, true), fibre.with(BlockModelGenerators.Y_ROT_180));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.WEST, true), fibre.with(BlockModelGenerators.Y_ROT_270));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.UP, true), fibre.with(BlockModelGenerators.X_ROT_270));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.DOWN, true), fibre.with(BlockModelGenerators.X_ROT_90));
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH1, true), growth1);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH2, true), growth2);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH3, true), growth3);
        gen.with(new ConditionBuilder().term(BlockTaintFibre.GROWTH4, true), growth4);
        blockModels.blockStateOutput.accept(gen);
    }

    private static MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }
}
