package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.infusion.BlockInfusionMatrix;
import com.leclowndu93150.thaumcraft.content.infusion.BlockPedestal;
import com.leclowndu93150.thaumcraft.content.infusion.BlockPillar;
import com.leclowndu93150.thaumcraft.content.decor.BlockCandle;
import com.leclowndu93150.thaumcraft.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumcraft.content.decor.banner.BannerWallBlock;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.content.crucible.BlockCrucible;
import com.leclowndu93150.thaumcraft.content.decor.BlockStairsTC;
import com.leclowndu93150.thaumcraft.content.decor.BlockStonePorous;
import com.leclowndu93150.thaumcraft.content.decor.BlockStoneTC;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJarVoid;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockAlembic;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTube;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeBuffer;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeFilter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeOneway;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeRestrict;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeValve;
import com.leclowndu93150.thaumcraft.content.infernalfurnace.BlockInfernalFurnace;
import com.leclowndu93150.thaumcraft.content.infernalfurnace.BlockPlaceholder;
import com.leclowndu93150.thaumcraft.content.metal.BlockMetalTC;
import com.leclowndu93150.thaumcraft.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumcraft.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintCrust;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintFeature;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintGeyser;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintLog;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintRock;
import com.leclowndu93150.thaumcraft.content.taint.block.BlockTaintSoil;
import com.leclowndu93150.thaumcraft.content.taint.flux.BlockFluxGoo;
import com.leclowndu93150.thaumcraft.content.taint.flux.FluxGooRefs;
import com.leclowndu93150.thaumcraft.content.workbench.BlockArcaneWorkbench;
import com.leclowndu93150.thaumcraft.content.workbench.BlockArcaneWorkbenchCharger;
import com.leclowndu93150.thaumcraft.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumcraft.content.world.plant.BlockGrassAmbient;
import com.leclowndu93150.thaumcraft.content.world.plant.BlockPlantCinderpearl;
import com.leclowndu93150.thaumcraft.content.world.plant.BlockPlantShimmerleaf;
import com.leclowndu93150.thaumcraft.content.world.plant.BlockPlantVishroom;
import com.leclowndu93150.thaumcraft.content.world.tree.BlockLeavesTC;
import com.leclowndu93150.thaumcraft.content.world.tree.BlockSaplingTC;
import com.leclowndu93150.thaumcraft.content.world.tree.TCTreeGrowers;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TCIds.MODID);

    public static final DeferredBlock<BlockResearchTable> RESEARCH_TABLE = BLOCKS.registerBlock(
            "research_table",
            BlockResearchTable::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockArcaneWorkbench> ARCANE_WORKBENCH = BLOCKS.registerBlock(
            "arcane_workbench",
            BlockArcaneWorkbench::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F,5.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockCrucible> CRUCIBLE = BLOCKS.registerBlock(
            "crucible",
            BlockCrucible::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(2.0F,20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockArcaneWorkbenchCharger> ARCANE_WORKBENCH_CHARGER = BLOCKS.registerBlock(
            "arcane_workbench_charger",
            BlockArcaneWorkbenchCharger::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(1.25F,10.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockAlembic> ALEMBIC = BLOCKS.registerBlock(
            "alembic",
            BlockAlembic::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2F,20.0F)
                    .sound(SoundType.WOOD)
                    .instrument(NoteBlockInstrument.BASEDRUM)
    );

    public static final DeferredBlock<BlockSmelter> SMELTER_BASIC = BLOCKS.registerBlock(
            "smelter_basic",
            BlockSmelter::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(2F,20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs->bs.getValue(BlockSmelter.LIT) ? 13 : 0)
    );

    public static final DeferredBlock<BlockSmelter> SMELTER_THAUMIUM = BLOCKS.registerBlock(
            "smelter_thaumium",
            BlockSmelter::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(2F,20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs->bs.getValue(BlockSmelter.LIT) ? 13 : 0)
    );

    public static final DeferredBlock<BlockSmelter> SMELTER_VOID = BLOCKS.registerBlock(
            "smelter_void",
            BlockSmelter::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(2F,20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs->bs.getValue(BlockSmelter.LIT) ? 13 : 0)
    );



    public static final DeferredBlock<BlockJar> JAR_NORMAL = BLOCKS.registerBlock(
            "jar_normal",
            BlockJar::new,
            props -> props
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TCSoundTypes.JAR.get())
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockJarVoid> JAR_VOID = BLOCKS.registerBlock(
            "jar_void",
            BlockJarVoid::new,
            props -> props
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TCSoundTypes.JAR.get())
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockTube> TUBE = BLOCKS.registerBlock(
            "tube",
            BlockTube::new,
            TCBlocks::tubeProps
    );

    public static final DeferredBlock<BlockTubeValve> TUBE_VALVE = BLOCKS.registerBlock(
            "tube_valve",
            BlockTubeValve::new,
            TCBlocks::tubeProps
    );

    public static final DeferredBlock<BlockTubeRestrict> TUBE_RESTRICT = BLOCKS.registerBlock(
            "tube_restrict",
            BlockTubeRestrict::new,
            TCBlocks::tubeProps
    );

    public static final DeferredBlock<BlockTubeFilter> TUBE_FILTER = BLOCKS.registerBlock(
            "tube_filter",
            BlockTubeFilter::new,
            TCBlocks::tubeProps
    );

    public static final DeferredBlock<BlockTubeOneway> TUBE_ONEWAY = BLOCKS.registerBlock(
            "tube_oneway",
            BlockTubeOneway::new,
            TCBlocks::tubeProps
    );

    public static final DeferredBlock<BlockTubeBuffer> TUBE_BUFFER = BLOCKS.registerBlock(
            "tube_buffer",
            BlockTubeBuffer::new,
            TCBlocks::tubeProps
    );

    public static final DeferredBlock<BlockFluxGoo> FLUX_GOO = BLOCKS.registerBlock(
            "flux_goo",
            props -> new BlockFluxGoo(FluxGooRefs.sourceFluid(), props),
            props -> props
                    .mapColor(MapColor.COLOR_PINK)
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .sound(TCSoundTypes.GORE.get())
                    .noLootTable()
                    .liquid()
                    .randomTicks()
    );

    public static final DeferredBlock<BlockTaintRock> TAINT_ROCK = BLOCKS.registerBlock(
            "taint_rock",
            BlockTaintRock::new,
            TCBlocks::taintBlockProps
    );

    public static final DeferredBlock<BlockTaintSoil> TAINT_SOIL = BLOCKS.registerBlock(
            "taint_soil",
            BlockTaintSoil::new,
            TCBlocks::taintBlockProps
    );

    public static final DeferredBlock<BlockTaintCrust> TAINT_CRUST = BLOCKS.registerBlock(
            "taint_crust",
            BlockTaintCrust::new,
            TCBlocks::taintBlockProps
    );

    public static final DeferredBlock<BlockTaintGeyser> TAINT_GEYSER = BLOCKS.registerBlock(
            "taint_geyser",
            BlockTaintGeyser::new,
            TCBlocks::taintBlockProps
    );

    public static final DeferredBlock<BlockTaintLog> TAINT_LOG = BLOCKS.registerBlock(
            "taint_log",
            BlockTaintLog::new,
            props -> props
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 100.0F)
                    .sound(TCSoundTypes.GORE.get())
                    .randomTicks()
                    .ignitedByLava()
    );

    public static final DeferredBlock<BlockTaintFeature> TAINT_FEATURE = BLOCKS.registerBlock(
            "taint_feature",
            BlockTaintFeature::new,
            props -> props
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.1F, 0.1F)
                    .sound(TCSoundTypes.GORE.get())
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
    );

    public static final DeferredBlock<BlockTaintFibre> TAINT_FIBRE = BLOCKS.registerBlock(
            "taint_fibre",
            BlockTaintFibre::new,
            props -> props
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0F)
                    .sound(TCSoundTypes.GORE.get())
                    .noOcclusion()
                    .noCollision()
                    .replaceable()
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .lightLevel(s -> {
                        if (s.getValue(BlockTaintFibre.GROWTH3)) return 12;
                        if (s.getValue(BlockTaintFibre.GROWTH2) || s.getValue(BlockTaintFibre.GROWTH4)) return 6;
                        return 0;
                    })
    );

    private static BlockBehaviour.Properties pedestalProps(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.STONE)
                .strength(2.0F, 17.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties pillarProps(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.STONE)
                .strength(2.0F, 17.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties taintBlockProps(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0F, 100.0F)
                .sound(TCSoundTypes.GORE.get())
                .noOcclusion()
                .randomTicks();
    }

    private static BlockBehaviour.Properties tubeProps(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    // 

    public static final DeferredBlock<Block> ORE_AMBER = BLOCKS.registerBlock(
            "ore_amber",
            Block::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> ORE_CINNABAR = BLOCKS.registerBlock(
            "ore_cinnabar",
            Block::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> ORE_QUARTZ = BLOCKS.registerBlock(
            "ore_quartz",
            Block::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final Map<DyeColor, DeferredBlock<BlockNitor>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, BLOCKS.registerBlock(
                    "nitor_" + dye.getName(),
                    props -> new BlockNitor(dye, props),
                    () -> nitorProps(dye)
            ));
        }
    }

    public static final Map<DyeColor, DeferredBlock<BlockCandle>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(dye, BLOCKS.registerBlock(
                    "candle_" + dye.getName(),
                    props -> new BlockCandle(dye, props),
                    () -> candleProps(dye)
            ));
        }
    }

    public static final Map<DyeColor, DeferredBlock<BannerStandingBlock>> BANNERS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<BannerWallBlock>> WALL_BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(dye, BLOCKS.registerBlock(
                    "banner_" + dye.getName(),
                    props -> new BannerStandingBlock(dye, props),
                    () -> bannerProps(dye)
            ));
            WALL_BANNERS.put(dye, BLOCKS.registerBlock(
                    "wall_banner_" + dye.getName(),
                    props -> new BannerWallBlock(dye, props),
                    () -> bannerProps(dye)
            ));
        }
    }

    public static final DeferredBlock<BannerStandingBlock> BANNER_CRIMSON_CULT = BLOCKS.registerBlock(
            "banner_crimson_cult",
            props -> new BannerStandingBlock(null, props),
            () -> bannerProps(null));

    public static final DeferredBlock<BannerWallBlock> WALL_BANNER_CRIMSON_CULT = BLOCKS.registerBlock(
            "wall_banner_crimson_cult",
            props -> new BannerWallBlock(null, props),
            () -> bannerProps(null));

    private static BlockBehaviour.Properties bannerProps(DyeColor dye) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
        return dye == null ? props.mapColor(MapColor.COLOR_RED) : props.mapColor(dye.getMapColor());
    }

    private static BlockBehaviour.Properties candleProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 14)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties nitorProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .noCollision()
                .pushReaction(PushReaction.DESTROY);
    }

    // 

    public static final DeferredBlock<BlockCrystal> CRYSTAL_AER = registerCrystal("crystal_aer", TCAspects.AER, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_IGNIS = registerCrystal("crystal_ignis", TCAspects.IGNIS, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_AQUA = registerCrystal("crystal_aqua", TCAspects.AQUA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_TERRA = registerCrystal("crystal_terra", TCAspects.TERRA, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_ORDO = registerCrystal("crystal_ordo", TCAspects.ORDO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_PERDITIO = registerCrystal("crystal_perditio", TCAspects.PERDITIO, false);
    public static final DeferredBlock<BlockCrystal> CRYSTAL_VITIUM = registerCrystal("crystal_vitium", TCAspects.VITIUM, true);

    private static DeferredBlock<BlockCrystal> registerCrystal(String name, ResourceKey<IAspect> aspect, boolean flux) {
        return BLOCKS.registerBlock(
                name,
                props -> new BlockCrystal(props, aspect, flux),
                props -> props
                        .mapColor(MapColor.NONE)
                        .strength(0.25F)
                        .sound(TCSoundTypes.CRYSTAL.get())
                        .lightLevel(state -> 1)
                        .noOcclusion()
                        .randomTicks()
                        .pushReaction(PushReaction.DESTROY)
        );
    }

    // 

    public static final DeferredBlock<BlockInfusionMatrix> INFUSION_MATRIX = BLOCKS.registerBlock(
            "infusion_matrix",
            BlockInfusionMatrix::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .noLootTable()
                    .noOcclusion()
                    .lightLevel(s -> 15)
    );

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ARCANE = BLOCKS.registerBlock(
            "pedestal_arcane", BlockPedestal::new, TCBlocks::pedestalProps);

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ANCIENT = BLOCKS.registerBlock(
            "pedestal_ancient", BlockPedestal::new, TCBlocks::pedestalProps);

    public static final DeferredBlock<BlockPedestal> PEDESTAL_ELDRITCH = BLOCKS.registerBlock(
            "pedestal_eldritch", BlockPedestal::new, TCBlocks::pedestalProps);

    public static final DeferredBlock<BlockPillar> PILLAR_ARCANE = BLOCKS.registerBlock(
            "pillar_arcane", BlockPillar::new, TCBlocks::pillarProps);

    public static final DeferredBlock<BlockPillar> PILLAR_ANCIENT = BLOCKS.registerBlock(
            "pillar_ancient", BlockPillar::new, TCBlocks::pillarProps);

    public static final DeferredBlock<BlockPillar> PILLAR_ELDRITCH = BLOCKS.registerBlock(
            "pillar_eldritch", BlockPillar::new, TCBlocks::pillarProps);

    public static final DeferredBlock<BlockStoneTC> STONE_ARCANE = BLOCKS.registerBlock(
            "stone_arcane",
            props -> new BlockStoneTC(props, false),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ARCANE_BRICK = BLOCKS.registerBlock(
            "stone_arcane_brick",
            props -> new BlockStoneTC(props, false),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT = BLOCKS.registerBlock(
            "stone_ancient",
            props -> new BlockStoneTC(props, false),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_TILE = BLOCKS.registerBlock(
            "stone_ancient_tile",
            props -> new BlockStoneTC(props, false),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_ROCK = BLOCKS.registerBlock(
            "stone_ancient_rock",
            props -> new BlockStoneTC(props, true),
            TCBlocks::unbreakableProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_GLYPHED = BLOCKS.registerBlock(
            "stone_ancient_glyphed",
            props -> new BlockStoneTC(props, false),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ANCIENT_DOORWAY = BLOCKS.registerBlock(
            "stone_ancient_doorway",
            props -> new BlockStoneTC(props, true),
            TCBlocks::unbreakableProps
    );

    public static final DeferredBlock<BlockStoneTC> STONE_ELDRITCH_TILE = BLOCKS.registerBlock(
            "stone_eldritch_tile",
            props -> new BlockStoneTC(props, false),
            TCBlocks::eldritchTileProps
    );

    public static final DeferredBlock<BlockStonePorous> STONE_POROUS = BLOCKS.registerBlock(
            "stone_porous",
            BlockStonePorous::new,
            TCBlocks::porousProps
    );

    public static final DeferredBlock<BlockStairsTC> STAIRS_ARCANE = BLOCKS.registerBlock(
            "stairs_arcane",
            props -> new BlockStairsTC(STONE_ARCANE.get().defaultBlockState(), props),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStairsTC> STAIRS_ARCANE_BRICK = BLOCKS.registerBlock(
            "stairs_arcane_brick",
            props -> new BlockStairsTC(STONE_ARCANE_BRICK.get().defaultBlockState(), props),
            TCBlocks::stoneProps
    );

    public static final DeferredBlock<BlockStairsTC> STAIRS_ANCIENT = BLOCKS.registerBlock(
            "stairs_ancient",
            props -> new BlockStairsTC(STONE_ANCIENT.get().defaultBlockState(), props),
            TCBlocks::stoneProps
    );

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties unbreakableProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchTileProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 1000.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties porousProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    // 

    public static final DeferredBlock<BlockSaplingTC> SAPLING_GREATWOOD = BLOCKS.registerBlock(
            "sapling_greatwood",
            props -> new BlockSaplingTC(TCTreeGrowers.GREATWOOD, props),
            props -> props
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final DeferredBlock<BlockSaplingTC> SAPLING_SILVERWOOD = BLOCKS.registerBlock(
            "sapling_silverwood",
            props -> new BlockSaplingTC(TCTreeGrowers.SILVERWOOD, props),
            props -> props
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final DeferredBlock<RotatedPillarBlock> LOG_GREATWOOD = BLOCKS.registerBlock(
            "log_greatwood",
            RotatedPillarBlock::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    public static final DeferredBlock<RotatedPillarBlock> LOG_SILVERWOOD = BLOCKS.registerBlock(
            "log_silverwood",
            RotatedPillarBlock::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava()
    );

    public static final DeferredBlock<BlockLeavesTC> LEAVES_GREATWOOD = BLOCKS.registerBlock(
            "leaves_greatwood",
            props -> new BlockLeavesTC(0.01F, props),
            TCBlocks::leavesProps
    );

    public static final DeferredBlock<BlockLeavesTC> LEAVES_SILVERWOOD = BLOCKS.registerBlock(
            "leaves_silverwood",
            props -> new BlockLeavesTC(0.01F, props),
            props -> leavesProps(props).mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> PLANK_GREATWOOD = BLOCKS.registerBlock(
            "plank_greatwood",
            Block::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    public static final DeferredBlock<Block> PLANK_SILVERWOOD = BLOCKS.registerBlock(
            "plank_silverwood",
            Block::new,
            props -> props
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
    );

    private static BlockBehaviour.Properties leavesProps(
            BlockBehaviour.Properties props
    ) {
        return props
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    // 

    public static final DeferredBlock<BlockPlantShimmerleaf> PLANT_SHIMMERLEAF = BLOCKS.registerBlock(
            "shimmerleaf",
            BlockPlantShimmerleaf::new,
            props -> props
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockPlantCinderpearl> PLANT_CINDERPEARL = BLOCKS.registerBlock(
            "cinderpearl",
            BlockPlantCinderpearl::new,
            props -> props
                    .mapColor(MapColor.COLOR_ORANGE)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 8)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockPlantVishroom> PLANT_VISHROOM = BLOCKS.registerBlock(
            "vishroom",
            BlockPlantVishroom::new,
            props -> props
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .noOcclusion()
    );

    public static final DeferredBlock<BlockGrassAmbient> GRASS_AMBIENT = BLOCKS.registerBlock(
            "grass_ambient",
            BlockGrassAmbient::new,
            props -> props
                    .mapColor(MapColor.GRASS)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .randomTicks()
    );

    // 

    public static final DeferredBlock<BlockMetalTC> METAL_THAUMIUM_BLOCK = BLOCKS.registerBlock(
            "metal_thaumium",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockMetalTC> METAL_BRASS_BLOCK = BLOCKS.registerBlock(
            "metal_brass",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockMetalTC> METAL_VOID_BLOCK = BLOCKS.registerBlock(
            "metal_void",
            BlockMetalTC::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> AMBER_BLOCK = BLOCKS.registerBlock(
            "amber_block",
            Block::new,
            props -> props
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockPlaceholder> OBSIDIAN_PLACEHOLDER = BLOCKS.registerBlock(
            "placeholder_obsidian",
            BlockPlaceholder::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockPlaceholder> NETHER_BRICKS_PLACEHOLDER = BLOCKS.registerBlock(
            "placeholder_nether_bricks",
            BlockPlaceholder::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<BlockInfernalFurnace> INFERNAL_FURNACE = BLOCKS.registerBlock(
            "infernal_furnace",
            BlockInfernalFurnace::new,
            props -> props
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
    );


    private TCBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
