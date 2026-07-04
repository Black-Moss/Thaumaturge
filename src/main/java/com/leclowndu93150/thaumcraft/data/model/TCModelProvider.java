package com.leclowndu93150.thaumcraft.data.model;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.color.AspectFilterTint;
import com.leclowndu93150.thaumcraft.client.model.JarItemSpecialRenderer;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumcraft.content.item.CelestialBody;
import com.leclowndu93150.thaumcraft.data.fragments.*;
import com.leclowndu93150.thaumcraft.data.model.crystal.CrystalBlockstateGenerator;
import com.leclowndu93150.thaumcraft.data.model.crystal.CrystalItemModelGenerator;
import com.leclowndu93150.thaumcraft.data.model.crystal.EssentiaCrystalModelGenerator;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TCModelProvider extends ModelProvider {
    private static final ModelTemplate THREE_LAYERED_ITEM = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("item/generated")),
            Optional.empty(),
            TextureSlot.LAYER0, TextureSlot.LAYER1, TextureSlot.LAYER2);

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
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.ARCANE_WORKBENCH.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.ARCANE_WORKBENCH.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.ARCANE_WORKBENCH_CHARGER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.ARCANE_WORKBENCH_CHARGER.get()))));
        registerAlembic(blockModels, itemModels, TCBlocks.ALEMBIC.get());
        registerSmelter(blockModels, itemModels,TCBlocks.SMELTER_BASIC.get(), "smelter_basic");
        registerSmelter(blockModels, itemModels,TCBlocks.SMELTER_THAUMIUM.get(), "smelter_thaumium");
        registerSmelter(blockModels, itemModels,TCBlocks.SMELTER_VOID.get(), "smelter_void");
        itemModels.generateFlatItem(TCItems.THAUMONOMICON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(TCItems.THAUMOMETER.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/thaumometer")));
        itemModels.generateFlatItem(TCItems.JAR_BRACE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.LABEL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BOTTLE_TAINT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VIS_RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIC_SLIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_CRAWLER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINTACLE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SWARM_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SEED_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.WISP_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BRAINY_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BRAIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FIREBAT_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MIND_SPIDER_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_VALVE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_RESTRICT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_ONEWAY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TUBE_BUFFER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOGGLES_REVEALING.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SCRIBING_TOOLS.get(), ModelTemplates.FLAT_ITEM);
        registerCelestialNotes(itemModels);
        registerCandles(blockModels, itemModels);
        registerBanners(blockModels, itemModels);
        itemModels.generateFlatItem(TCItems.TALLOW.get(), ModelTemplates.FLAT_ITEM);
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

    private void registerBanners(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/tc_banner");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        Material stand = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_stand"));
        Material cloth = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_cloth"));
        Material symbol = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_symbol"));
        Identifier dyedItemModel = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_dyed");
        Identifier cultistItemModel = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/banner_cultist");
        THREE_LAYERED_ITEM.create(dyedItemModel,
                TextureMapping.layered(stand, cloth, symbol), itemModels.modelOutput);
        ModelTemplates.TWO_LAYERED_ITEM.create(cultistItemModel,
                TextureMapping.layered(stand, new Material(cultistItemModel)), itemModels.modelOutput);
        for (DyeColor dye : DyeColor.values()) {
            blockModels.blockStateOutput.accept(
                    BlockModelGenerators.createSimpleBlock(TCBlocks.BANNERS.get(dye).get(), variant));
            blockModels.blockStateOutput.accept(
                    BlockModelGenerators.createSimpleBlock(TCBlocks.WALL_BANNERS.get(dye).get(), variant));
            int tint = 0xFF000000 | dye.getMapColor().col;
            itemModels.itemModelOutput.accept(TCItems.BANNERS.get(dye).get(),
                    ItemModelUtils.tintedModel(dyedItemModel,
                            new Constant(0xFFFFFFFF),
                            new Constant(tint),
                            new AspectFilterTint(dye.getMapColor().col)));
        }
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TCBlocks.BANNER_CRIMSON_CULT.get(), variant));
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(TCBlocks.WALL_BANNER_CRIMSON_CULT.get(), variant));
        itemModels.itemModelOutput.accept(TCItems.BANNER_CRIMSON_CULT.get(),
                ItemModelUtils.plainModel(cultistItemModel));
    }

    private void registerCandles(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/candle");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        for (DyeColor dye : DyeColor.values()) {
            Block candle = TCBlocks.CANDLES.get(dye).get();
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(candle, variant));
            int tint = 0xFF000000 | dye.getMapColor().col;
            itemModels.itemModelOutput.accept(candle.asItem(),
                    ItemModelUtils.tintedModel(model, new Constant(tint)));
        }
    }

    private void registerCelestialNotes(ItemModelGenerators itemModels) {
        Material sheet = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/celestial_notes_sheet"));
        List<SelectItemModel.SwitchCase<CelestialBody>> cases = new ArrayList<>();
        for (CelestialBody body : CelestialBody.values()) {
            Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/celestial_notes_" + body.getSerializedName());
            ModelTemplates.TWO_LAYERED_ITEM.create(model,
                    TextureMapping.layered(sheet, new Material(model)), itemModels.modelOutput);
            cases.add(ItemModelUtils.when(body, ItemModelUtils.plainModel(model)));
        }
        Identifier fallback = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/celestial_notes_sun");
        itemModels.itemModelOutput.accept(TCItems.CELESTIAL_NOTES.get(),
                ItemModelUtils.select(new ComponentContents<>(TCDataComponents.CELESTIAL_BODY.get()),
                        ItemModelUtils.plainModel(fallback), cases));
    }

    private void registerJar(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(
                        block,
                        BlockModelGenerators.plainVariant(TCIds.rl("block/" + modelName))
                )
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

    private void registerAlembic(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block) {

        MultiVariant coreVariant = variantOf("alembic");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block).with(coreVariant);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BooleanProperty property = BlockEssentiaTransport.propertyFor(direction);
            Variant rotated = applyRotation(new Variant(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/alembic_bore")), direction);
            MultiVariant variant = new MultiVariant(WeightedList.of(rotated));
            generator = generator.with(new ConditionBuilder().term(property, true), variant);
        }
        blockModels.blockStateOutput.accept(generator);

        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(
                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/alembic"),
                Optional.empty(),
                List.of()
        ));

    }

    private static Variant applyRotation(Variant base, Direction direction) {
        VariantMutator mutator = switch (direction) {
            case DOWN -> BlockModelGenerators.NOP;
            case UP -> BlockModelGenerators.X_ROT_180;
            case NORTH -> BlockModelGenerators.X_ROT_270;
            case SOUTH -> BlockModelGenerators.X_ROT_90;
            case WEST -> BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_270);
            case EAST -> BlockModelGenerators.X_ROT_270.then(BlockModelGenerators.Y_ROT_90);
        };
        return mutator.apply(base);
    }

    private void registerSmelter(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName) {
        MultiVariant off = variantOf(modelName+"_off");
        MultiVariant on = variantOf(modelName + "_on");
        PropertyDispatch<MultiVariant> lit = PropertyDispatch.initial(BlockSmelter.LIT)
                .select(false, off)
                .select(true, on);
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(lit).with(rotations)
        );

        itemModels.itemModelOutput.accept(block.asItem(), new CuboidItemModelWrapper.Unbaked(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName + "_off"), Optional.empty(), List.of()));



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
