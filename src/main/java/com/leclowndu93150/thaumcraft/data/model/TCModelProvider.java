package com.leclowndu93150.thaumcraft.data.model;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.model.JarItemSpecialRenderer;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumcraft.data.fragments.*;
import com.leclowndu93150.thaumcraft.data.model.crystal.CrystalBlockstateGenerator;
import com.leclowndu93150.thaumcraft.data.model.crystal.CrystalItemModelGenerator;
import com.leclowndu93150.thaumcraft.data.model.crystal.EssentiaCrystalModelGenerator;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;

public final class TCModelProvider extends ModelProvider {
    public TCModelProvider(PackOutput output) {
        super(output, TCIds.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerResearchTable(blockModels);
        registerJar(blockModels, itemModels, TCBlocks.JAR_NORMAL.get(), "jar_normal");
        registerJar(blockModels, itemModels, TCBlocks.JAR_VOID.get(), "jar_void");
        TubeModels.register(blockModels);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.CRUCIBLE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.CRUCIBLE.get()))));
        itemModels.generateFlatItem(TCItems.THAUMONOMICON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.JAR_BRACE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.LABEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BOTTLE_TAINT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIC_SLIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_CRAWLER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINTACLE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SWARM_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SEED_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_VALVE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_RESTRICT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_ONEWAY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_BUFFER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOGGLES_REVEALING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMOMETER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SCRIBING_TOOLS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_QUARTZ.get(), ModelTemplates.FLAT_ITEM);
        TCBlocksAOresModels.register(blockModels, itemModels);
        CrystalBlockstateGenerator.register(blockModels);
        CrystalItemModelGenerator.register(itemModels);
        EssentiaCrystalModelGenerator.register(itemModels);
        TCBlocksCStoneModels.register(blockModels);
        TCBlocksFMetalsModels.register(blockModels, itemModels);
        TCBlocksDTreesModels.register(blockModels, itemModels);
        TCBlocksEPlantsModels.register(blockModels, itemModels);
        TCBlocksTaintModels.register(blockModels, itemModels);
        TCItemsHContainersModels.register(itemModels);
    }

    private void registerJar(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        MultiVariant empty = variantOf(modelName);
        MultiVariant fill25 = variantOf(modelName + "_25");
        MultiVariant fill50 = variantOf(modelName + "_50");
        MultiVariant fill75 = variantOf(modelName + "_75");
        MultiVariant fill100 = variantOf(modelName + "_100");
        PropertyDispatch<MultiVariant> fillLevels = PropertyDispatch.initial(BlockJar.FILL_LEVEL)
                .select(0, empty)
                .select(1, fill25)
                .select(2, fill50)
                .select(3, fill75)
                .select(4, fill100);
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, empty)/*.with(fillLevels)*/.with(rotations)
        );

        itemModels.itemModelOutput.accept(block.asItem(), new CompositeModel.Unbaked(
                List.of(
                        new CuboidItemModelWrapper.Unbaked(
                                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName),
                                Optional.empty(),
                                List.of()
                        ),
                        new SpecialModelWrapper.Unbaked(
                                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName),
                                Optional.empty(),
                                new JarItemSpecialRenderer.Unbaked()
                        )
                ),
                Optional.empty()
        ));
    }

    private MultiVariant variantOf(String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        return new MultiVariant(WeightedList.of(new Variant(model)));
    }

    private void registerResearchTable(BlockModelGenerators blockModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/research_table");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.RESEARCH_TABLE.get(), variant).with(rotations));
    }
}
