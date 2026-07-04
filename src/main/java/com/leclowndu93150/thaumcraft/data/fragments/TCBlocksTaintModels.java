package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;

public final class TCBlocksTaintModels {
    private TCBlocksTaintModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                TCBlocks.TAINT_ROCK.get(), rotatedWeighted(new String[]{"taint_rock"}, new int[]{1})));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                TCBlocks.TAINT_SOIL.get(),
                rotatedWeighted(new String[]{"taint_soil_0", "taint_soil_1", "taint_soil_2"}, new int[]{16, 1, 1})));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(
                TCBlocks.TAINT_CRUST.get(),
                rotatedWeighted(new String[]{"taint_crust_0", "taint_crust_1", "taint_crust_2"}, new int[]{8, 1, 1})));

        registerFluxGoo(blockModels);
        registerTaintGeyser(blockModels);
        registerTaintLog(blockModels);
        registerTaintFeature(blockModels);
        registerTaintFibre(blockModels);

        blockItemModel(itemModels, TCBlocks.TAINT_ROCK.asItem(), "taint_rock");
        blockItemModel(itemModels, TCBlocks.TAINT_SOIL.asItem(), "taint_soil_0");
        blockItemModel(itemModels, TCBlocks.TAINT_CRUST.asItem(), "taint_crust_0");
        blockItemModel(itemModels, TCBlocks.TAINT_GEYSER.asItem(), "taint_geyser");
        blockItemModel(itemModels, TCBlocks.TAINT_LOG.asItem(), "taint_log");
        blockItemModel(itemModels, TCBlocks.TAINT_FEATURE.asItem(), "taint_orb_0");
        blockItemModel(itemModels, TCBlocks.TAINT_FIBRE.asItem(), "taint_fibre");
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
        WeightedList.Builder<Variant> entries = WeightedList.builder();
        for (int tex = 1; tex <= 2; tex++) {
            for (String face : new String[]{"north", "south", "east", "west"}) {
                entries.add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID,
                        "block/taint_log_" + face + tex)), 1);
            }
        }
        MultiVariant barks = new MultiVariant(entries.build());
        PropertyDispatch<VariantMutator> axes = PropertyDispatch.modify(BlockStateProperties.AXIS)
                .select(Direction.Axis.Y, BlockModelGenerators.NOP)
                .select(Direction.Axis.Z, BlockModelGenerators.X_ROT_90)
                .select(Direction.Axis.X, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TCBlocks.TAINT_LOG.get(), barks).with(axes));
    }

    private static MultiVariant rotatedWeighted(String[] models, int[] weights) {
        WeightedList.Builder<Variant> entries = WeightedList.builder();
        for (int i = 0; i < models.length; i++) {
            Variant base = new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + models[i]));
            entries.add(base, weights[i]);
            entries.add(BlockModelGenerators.X_ROT_90.apply(base), weights[i]);
            entries.add(BlockModelGenerators.Y_ROT_90.apply(base), weights[i]);
            entries.add(BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90).apply(base), weights[i]);
        }
        return new MultiVariant(entries.build());
    }

    private static void registerTaintFeature(BlockModelGenerators blockModels) {
        MultiVariant orbs = orbVariants();
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(DirectionalBlock.FACING)
                .select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180)
                .select(Direction.NORTH, BlockModelGenerators.X_ROT_270)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(TCBlocks.TAINT_FEATURE.get(), orbs).with(rotations));
    }

    private static MultiVariant orbVariants() {
        return new MultiVariant(WeightedList.<Variant>builder()
                .add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_orb_0")))
                .add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_orb_1")))
                .add(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/taint_orb_2")))
                .build());
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

    private static void blockItemModel(ItemModelGenerators itemModels, Item item, String modelName) {
        itemModels.itemModelOutput.accept(item,
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName)));
    }

    private static MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }
}
