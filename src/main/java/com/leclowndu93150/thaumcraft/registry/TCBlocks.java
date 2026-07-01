package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJarVoid;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTube;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeBuffer;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeFilter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeOneway;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeRestrict;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockTubeValve;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    private static BlockBehaviour.Properties taintBlockProps(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0F, 100.0F)
                .sound(TCSoundTypes.GORE.get())
                .randomTicks();
    }

    private static BlockBehaviour.Properties tubeProps(BlockBehaviour.Properties props) {
        return props
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private TCBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
