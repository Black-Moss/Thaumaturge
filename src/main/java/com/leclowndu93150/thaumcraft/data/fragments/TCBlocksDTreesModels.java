package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class TCBlocksDTreesModels {
    private TCBlocksDTreesModels() {}

    public static void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        simpleCube(blockModels, TCBlocks.SAPLING_GREATWOOD.get(), "sapling_greatwood");
        simpleCube(blockModels, TCBlocks.SAPLING_SILVERWOOD.get(), "sapling_silverwood");
        TCBlocksEPlantsModels.flatItemFromBlock(itemModels, TCItems.SAPLING_GREATWOOD.get(), TCBlocks.SAPLING_GREATWOOD.get());
        TCBlocksEPlantsModels.flatItemFromBlock(itemModels, TCItems.SAPLING_SILVERWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get());
        simpleCube(blockModels, TCBlocks.PLANK_GREATWOOD.get(), "plank_greatwood");
        simpleCube(blockModels, TCBlocks.PLANK_SILVERWOOD.get(), "plank_silverwood");
        simpleCube(blockModels, TCBlocks.LEAVES_GREATWOOD.get(), "leaves_greatwood");
        simpleCube(blockModels, TCBlocks.LEAVES_SILVERWOOD.get(), "leaves_silverwood");
        log(blockModels, TCBlocks.LOG_GREATWOOD.get(), "log_greatwood", "log_greatwood_horizontal");
        log(blockModels, TCBlocks.LOG_SILVERWOOD.get(), "log_silverwood", "log_silverwood_horizontal");
    }

    private static void simpleCube(BlockModelGenerators blockModels, Block block, String modelName) {
        MultiVariant variant = variantOf(modelName);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

    private static void log(BlockModelGenerators blockModels, Block block, String verticalName, String horizontalName) {
        MultiVariant vertical = variantOf(verticalName);
        MultiVariant horizontal = variantOf(horizontalName);
        PropertyDispatch<MultiVariant> dispatch = PropertyDispatch.initial(BlockStateProperties.AXIS)
                .select(Direction.Axis.Y, vertical)
                .select(Direction.Axis.Z, horizontal.with(BlockModelGenerators.X_ROT_90))
                .select(Direction.Axis.X, horizontal.with(BlockModelGenerators.X_ROT_90).with(BlockModelGenerators.Y_ROT_90));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private static MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }
}
