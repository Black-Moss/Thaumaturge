package com.leclowndu93150.thaumcraft.data.model;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.device.BlockVisBattery;
import com.leclowndu93150.thaumcraft.client.color.AspectFilterTint;
import com.leclowndu93150.thaumcraft.client.color.FocusColorTint;
import com.leclowndu93150.thaumcraft.client.color.GolemMaterialTint;
import com.leclowndu93150.thaumcraft.client.model.CentrifugeItemSpecialRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import com.leclowndu93150.thaumcraft.client.model.JarBrainItemSpecialRenderer;
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
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
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
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TCModelProvider extends ModelProvider {
    private static final int ROBES_UNDYED_ARGB = 0xFF6A3880;

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
        registerJarBrain(blockModels, itemModels);
        registerAuraDevices(blockModels, itemModels);
        registerNoiseDevices(blockModels, itemModels);
        TubeModels.register(blockModels);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.CRUCIBLE.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.CRUCIBLE.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.ARCANE_WORKBENCH.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.ARCANE_WORKBENCH.get()))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.ARCANE_WORKBENCH_CHARGER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(TCBlocks.ARCANE_WORKBENCH_CHARGER.get()))));
        horizontalBlock(blockModels, itemModels, TCBlocks.INFERNAL_FURNACE.get(), "infernal_furnace",true);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(Blocks.NETHER_BRICKS))));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(TCBlocks.OBSIDIAN_PLACEHOLDER.get(), BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(Blocks.OBSIDIAN))));
        registerAlembic(blockModels, itemModels, TCBlocks.ALEMBIC.get());
        registerSmelter(blockModels, itemModels,TCBlocks.SMELTER_BASIC.get(), "smelter_basic");
        registerSmelter(blockModels, itemModels,TCBlocks.SMELTER_THAUMIUM.get(), "smelter_thaumium");
        registerSmelter(blockModels, itemModels,TCBlocks.SMELTER_VOID.get(), "smelter_void");
        horizontalBlock(blockModels, itemModels, TCBlocks.SMELTER_AUX.get(), "smelter_aux");
        horizontalBlock(blockModels, itemModels, TCBlocks.SMELTER_VENT.get(), "smelter_vent");
        itemModels.generateFlatItem(TCItems.THAUMONOMICON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SALIS_MUNDUS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FABRIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MIRRORED_GLASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MECHANISM_SIMPLE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MECHANISM_COMPLEX.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MORPHIC_RESONATOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.BATH_SALTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SANITY_SOAP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_BEEF.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_CHICKEN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_PORK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_FISH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_RABBIT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CHUNK_MUTTON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.TRIPLE_MEAT_TREAT.get(), ModelTemplates.FLAT_ITEM);
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
        itemModels.generateFlatItem(TCItems.ALUMENTUM.get(), ModelTemplates.FLAT_ITEM);
        registerCelestialNotes(itemModels);

        for (DyeColor dye : DyeColor.values()) {
            registerNitor(blockModels, itemModels, dye);
        }

        // Resources
        blockModels.createTrivialCube(TCBlocks.ORE_AMBER.get());
        blockModels.createTrivialCube(TCBlocks.ORE_CINNABAR.get());
        blockModels.createTrivialCube(TCBlocks.ORE_QUARTZ.get());

        blockModels.createTrivialCube(TCBlocks.ALCHEMICAL_CONSTRUCT.get());
        blockModels.createTrivialCube(TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());

        blockModels.createTrivialCube(TCBlocks.METAL_BRASS_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.METAL_VOID_BLOCK.get());
        blockModels.createTrivialCube(TCBlocks.AMBER_BLOCK.get());

        itemModels.generateFlatItem(TCItems.INGOT_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.INGOT_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.AMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.QUICKSILVER.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.RARE_EARTH.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.NUGGET_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_VOID.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.NUGGET_QUICKSILVER.get(), ModelTemplates.FLAT_ITEM);
        registerInfusionAltar(blockModels, itemModels);
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.FOCAL_MANIPULATOR.get(), "focal_manipulator");
        registerInvisibleBlock(blockModels, TCBlocks.HOLE.get());
        registerInvisibleBlock(blockModels, TCBlocks.EFFECT_SAP.get());
        registerInvisibleBlock(blockModels, TCBlocks.EFFECT_GLIMMER.get());

        registerCandles(blockModels, itemModels);
        registerBanners(blockModels, itemModels);
        itemModels.generateFlatItem(TCItems.TALLOW.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.ELEMENTAL_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.PRIMAL_CRUSHER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(TCItems.TRAVELLER_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.THAUMIUM_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_HELM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_CHEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_LEGS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.VOID_BOOTS.get(), ModelTemplates.FLAT_ITEM);
        registerRobeItem(itemModels, TCItems.CLOTH_CHEST.get(), "cloth_chest");
        registerRobeItem(itemModels, TCItems.CLOTH_LEGS.get(), "cloth_legs");
        registerRobeItem(itemModels, TCItems.CLOTH_BOOTS.get(), "cloth_boots");
        registerSpa(blockModels, itemModels);
        registerCasters(itemModels);
        registerGolemancy(blockModels, itemModels);

        itemModels.generateFlatItem(TCItems.NUGGET_QUARTZ.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.VOID_SEED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CAUSALITY_COLLAPSER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_GOLD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_COPPER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_LEAD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_TIN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_CINNABAR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.CLUSTER_QUARTZ.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(TCItems.PLATE_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PLATE_THAUMIUM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PLATE_BRASS.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.PLATE_VOID.get(), ModelTemplates.FLAT_ITEM);


        CrystalBlockstateGenerator.register(blockModels);
        CrystalItemModelGenerator.register(itemModels);
        EssentiaCrystalModelGenerator.register(itemModels);
        TCBlocksCStoneModels.register(blockModels);
        TCBlocksDTreesModels.register(blockModels, itemModels);
        TCBlocksEPlantsModels.register(blockModels, itemModels);
        TCBlocksTaintModels.register(blockModels, itemModels);
        TCItemsHContainersModels.register(itemModels);
    }

    private void horizontalBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName){
        horizontalBlock(blockModels, itemModels, block, modelName, false);

    }
    private void horizontalBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, Block block, String modelName,boolean oversizedInGui){
        PropertyDispatch<VariantMutator> rotations = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block,variantOf(modelName)).with(rotations)
        );
        itemModels.itemModelOutput.accept(block.asItem(),
                new CuboidItemModelWrapper.Unbaked(
                        Identifier.fromNamespaceAndPath(TCIds.MODID,"block/"+modelName),
                        Optional.empty(),
                        List.of()
                ), new ClientItem.Properties(true,oversizedInGui,1));

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

    private static void registerInvisibleBlock(BlockModelGenerators blockModels, Block block) {
        Identifier empty = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/empty");
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(empty)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
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


    private static void registerInfusionAltar(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> facing = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .select(Direction.NORTH, BlockModelGenerators.NOP)
                .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_270);
        registerPillar(blockModels, itemModels, TCBlocks.PILLAR_ARCANE.get(), "pillar_arcane", facing);
        registerPillar(blockModels, itemModels, TCBlocks.PILLAR_ANCIENT.get(), "pillar_ancient", facing);
        registerPillar(blockModels, itemModels, TCBlocks.PILLAR_ELDRITCH.get(), "pillar_eldritch", facing);
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.PEDESTAL_ARCANE.get(), "pedestal_arcane");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.RECHARGE_PEDESTAL.get(), "recharge_pedestal");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.PEDESTAL_ANCIENT.get(), "pedestal_ancient");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.PEDESTAL_ELDRITCH.get(), "pedestal_eldritch");
        registerSimpleWithItem(blockModels, itemModels, TCBlocks.INFUSION_MATRIX.get(), "infusion_matrix");
        itemModels.itemModelOutput.accept(TCBlocks.INFUSION_MATRIX.asItem(), ItemModelUtils.plainModel(
                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/infusion_matrix")));
    }

    private static void registerPillar(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
                                       Block block, String modelName, PropertyDispatch<VariantMutator> facing) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant).with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
    }

    private static void registerSimpleWithItem(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
                                               Block block, String modelName) {
        Identifier model = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + modelName);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(model)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
        if (block != TCBlocks.INFUSION_MATRIX.get()) {
            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
        }
    }


    private static void registerSpa(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier spaModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                ModelLocationUtils.getModelLocation(TCBlocks.SPA.get()),
                new TextureMapping()
                        .put(TextureSlot.SIDE, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/spa_side")))
                        .put(TextureSlot.TOP, new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "block/spa_top")))
                        .put(TextureSlot.BOTTOM, new Material(Identifier.withDefaultNamespace("block/furnace_top"))),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.SPA.get(),
                new MultiVariant(WeightedList.of(new Variant(spaModel)))));
        itemModels.itemModelOutput.accept(TCItems.SPA.get(), ItemModelUtils.plainModel(spaModel));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.PURIFYING_FLUID.get(),
                new MultiVariant(WeightedList.of(new Variant(
                        Identifier.fromNamespaceAndPath(TCIds.MODID, "block/purifying_fluid"))))));
    }

    private static void registerGolemancy(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(TCItems.MIND_CLOCKWORK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MIND_BIOTHAUMIC.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MODULE_VISION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.MODULE_AGGRESSION.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.GOLEM_BELL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BLANK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_PICKUP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_PICKUP_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_FILL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_FILL_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_EMPTY.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_EMPTY_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_HARVEST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BUTCHER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_GUARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_GUARD_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_LUMBER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BREAKER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_BREAKER_ADVANCED.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_USE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_PROVIDER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(TCItems.SEAL_STOCK.get(), ModelTemplates.FLAT_ITEM);

        Identifier golemModel = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(TCItems.GOLEM_PLACER.get()),
                TextureMapping.layer0(TCItems.GOLEM_PLACER.get()), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TCItems.GOLEM_PLACER.get(),
                ItemModelUtils.tintedModel(golemModel, new GolemMaterialTint()));

        PropertyDispatch<VariantMutator> levitatorFacing = PropertyDispatch.modify(BlockStateProperties.FACING)
                .select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180)
                .select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        Identifier levitatorOn = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/levitator_on");
        Identifier levitatorOff = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/levitator_off");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.LEVITATOR.get())
                .with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                        .select(true, new MultiVariant(WeightedList.of(new Variant(levitatorOn))))
                        .select(false, new MultiVariant(WeightedList.of(new Variant(levitatorOff)))))
                .with(levitatorFacing));
        itemModels.itemModelOutput.accept(TCItems.LEVITATOR.get(), ItemModelUtils.plainModel(levitatorOff));

        registerInvisibleBlock(blockModels, TCBlocks.GOLEM_BUILDER.get());
        itemModels.itemModelOutput.accept(TCItems.GOLEM_BUILDER.get(), ItemModelUtils.plainModel(
                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/empty")));
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_IRON_BARS.get());
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_CAULDRON.get());
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_ANVIL.get());
        registerInvisibleBlock(blockModels, TCBlocks.PLACEHOLDER_SMITHING_TABLE.get());
    }

    private static void registerCasters(ItemModelGenerators itemModels) {
        ItemModel.Unbaked bare = ItemModelUtils.plainModel(
                Identifier.fromNamespaceAndPath(TCIds.MODID, "item/caster_basic_model"));
        ItemModel.Unbaked socketed = ItemModelUtils.tintedModel(
                Identifier.fromNamespaceAndPath(TCIds.MODID, "item/caster_basic_focus_model"),
                new Constant(0xFFFFFF), new FocusColorTint());
        itemModels.itemModelOutput.accept(TCItems.CASTER_BASIC.get(),
                ItemModelUtils.conditional(
                        ItemModelUtils.hasComponent(TCDataComponents.SOCKETED_FOCUS.get()),
                        socketed,
                        bare));
        registerFocusItem(itemModels, TCItems.FOCUS_1.get());
        registerFocusItem(itemModels, TCItems.FOCUS_2.get());
        registerFocusItem(itemModels, TCItems.FOCUS_3.get());
    }

    private static void registerFocusItem(ItemModelGenerators itemModels, Item item) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item),
                TextureMapping.layer0(item), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(model, new FocusColorTint()));
    }

    private static void registerRobeItem(ItemModelGenerators itemModels, Item item, String name) {
        Identifier itemModelId = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name);
        Material baseTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name));
        Material overTex = new Material(Identifier.fromNamespaceAndPath(TCIds.MODID, "item/" + name + "_over"));
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModelId, TextureMapping.layered(baseTex, overTex), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(itemModelId, new Dye(ROBES_UNDYED_ARGB)));
    }



    private void registerJarBrain(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(
                        TCBlocks.JAR_BRAIN.get(),
                        BlockModelGenerators.plainVariant(TCIds.rl("block/jar_normal"))
                )
        );
        itemModels.itemModelOutput.accept(TCBlocks.JAR_BRAIN.get().asItem(), new CompositeModel.Unbaked(
                List.of(
                        new CuboidItemModelWrapper.Unbaked(
                                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/jar_normal"),
                                Optional.empty(),
                                List.of()
                        ),
                        new SpecialModelWrapper.Unbaked(
                                Identifier.fromNamespaceAndPath(TCIds.MODID, "block/jar_normal"),
                                Optional.empty(),
                                new JarBrainItemSpecialRenderer.Unbaked()
                        )
                ),
                Optional.empty()
        ));
    }

    private void registerNoiseDevices(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        PropertyDispatch<VariantMutator> wallMount = PropertyDispatch.modify(BlockStateProperties.FACING)
                .select(Direction.UP, BlockModelGenerators.NOP)
                .select(Direction.DOWN, BlockModelGenerators.X_ROT_180)
                .select(Direction.NORTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.ARCANE_EAR.get(),
                "arcane_ear_on", "arcane_ear_off", wallMount);
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.ARCANE_EAR_TOGGLE.get(),
                "arcane_ear_toggle_on", "arcane_ear_toggle_off", wallMount);

        PropertyDispatch<VariantMutator> hangMount = PropertyDispatch.modify(BlockStateProperties.FACING)
                .select(Direction.DOWN, BlockModelGenerators.NOP)
                .select(Direction.UP, BlockModelGenerators.X_ROT_180)
                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90)
                .select(Direction.NORTH, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_180))
                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_270))
                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90));
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.LAMP_ARCANE.get(),
                "lamp_arcane_on", "lamp_arcane_off", hangMount);
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.LAMP_GROWTH.get(),
                "lamp_growth_on", "lamp_growth_off", hangMount);
        registerEnabledFacingDevice(blockModels, itemModels, TCBlocks.LAMP_FERTILITY.get(),
                "lamp_fertility_on", "lamp_fertility_off", hangMount);

        registerInvisibleBlock(blockModels, TCBlocks.CENTRIFUGE.get());
        itemModels.itemModelOutput.accept(TCItems.CENTRIFUGE.get(), new SpecialModelWrapper.Unbaked(
                Identifier.fromNamespaceAndPath(TCIds.MODID, "item/centrifuge_base"),
                Optional.empty(),
                new CentrifugeItemSpecialRenderer.Unbaked()
        ));

        registerInvisibleBlock(blockModels, TCBlocks.HUNGRY_CHEST.get());
        itemModels.itemModelOutput.accept(TCItems.HUNGRY_CHEST.get(), new SpecialModelWrapper.Unbaked(
                Identifier.withDefaultNamespace("item/chest"),
                Optional.empty(),
                new ChestSpecialRenderer.Unbaked(TCIds.rl("hungry"))
        ));
    }

    private void registerEnabledFacingDevice(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
                                             Block block, String onModel, String offModel,
                                             PropertyDispatch<VariantMutator> facing) {
        Identifier on = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + onModel);
        Identifier off = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + offModel);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                        .select(true, new MultiVariant(WeightedList.of(new Variant(on))))
                        .select(false, new MultiVariant(WeightedList.of(new Variant(off)))))
                .with(facing));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(off));
    }

    private void registerAuraDevices(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(TCBlocks.MATRIX_SPEED.get());
        blockModels.createTrivialCube(TCBlocks.MATRIX_COST.get());

        Identifier[] batteryModels = new Identifier[5];
        for (int i = 0; i < 5; i++) {
            Identifier textureId = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/vis_battery_" + i);
            batteryModels[i] = ModelTemplates.CUBE_ALL.create(
                    Identifier.fromNamespaceAndPath(TCIds.MODID, "block/vis_battery_" + i),
                    TextureMapping.cube(new Material(textureId)), blockModels.modelOutput);
        }
        PropertyDispatch<MultiVariant> chargeDispatch = PropertyDispatch.initial(BlockVisBattery.CHARGE)
                .generate(charge -> {
                    int tier = charge == 0 ? 0 : charge >= 10 ? 4 : (charge + 2) / 3;
                    return new MultiVariant(WeightedList.of(new Variant(batteryModels[tier])));
                });
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.VIS_BATTERY.get()).with(chargeDispatch));
        itemModels.itemModelOutput.accept(TCItems.VIS_BATTERY.get(), ItemModelUtils.plainModel(batteryModels[0]));

        Identifier dioptraOn = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/dioptra_on");
        Identifier dioptraOff = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/dioptra_off");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TCBlocks.DIOPTRA.get())
                .with(PropertyDispatch.initial(BlockStateProperties.ENABLED)
                        .select(true, new MultiVariant(WeightedList.of(new Variant(dioptraOn))))
                        .select(false, new MultiVariant(WeightedList.of(new Variant(dioptraOff))))));
        itemModels.itemModelOutput.accept(TCItems.DIOPTRA.get(), ItemModelUtils.plainModel(dioptraOn));
    }

    private static void cubeAllTexture(BlockModelGenerators blockModels, Block block, String textureName) {
        Identifier textureId = Identifier.fromNamespaceAndPath(TCIds.MODID, "block/" + textureName);
        Material texture = new Material(textureId);
        Identifier modelId = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(texture), blockModels.modelOutput);
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
    }

}
