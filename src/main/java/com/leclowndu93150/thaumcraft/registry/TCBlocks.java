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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
