package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJarVoid;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeOneway;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeRestrict;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeValve;
import com.leclowndu93150.thaumcraft.content.misc.nitor.BlockEntityNitor;
import com.leclowndu93150.thaumcraft.content.research.table.BlockEntityResearchTable;
import java.util.Set;
import java.util.stream.Collectors;

import com.leclowndu93150.thaumcraft.content.workbench.BlockEntityArcaneWorkbench;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TCIds.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityResearchTable>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register(
                    "research_table",
                    () -> new BlockEntityType<>(BlockEntityResearchTable::new, Set.of(TCBlocks.RESEARCH_TABLE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityArcaneWorkbench>> ARCANE_WORKBENCH =
            BLOCK_ENTITIES.register(
                    "arcane_workbench",
                    () -> new BlockEntityType<>(BlockEntityArcaneWorkbench::new, Set.of(TCBlocks.ARCANE_WORKBENCH.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityCrucible>> CRUCIBLE =
            BLOCK_ENTITIES.register(
                    "crucible",
                    () -> new BlockEntityType<>(BlockEntityCrucible::new, Set.of(TCBlocks.CRUCIBLE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySmelter>> SMELTER =
            BLOCK_ENTITIES.register(
                    "smelter",
                    () -> new BlockEntityType<>(BlockEntitySmelter::new, Set.of(TCBlocks.SMELTER_BASIC.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityAlembic>> ALEMBIC =
            BLOCK_ENTITIES.register(
                    "alembic",
                    () -> new BlockEntityType<>(BlockEntityAlembic::new, Set.of(TCBlocks.ALEMBIC.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJar>> JAR =
            BLOCK_ENTITIES.register(
                    "jar",
                    () -> new BlockEntityType<>(BlockEntityJar::new, Set.of(TCBlocks.JAR_NORMAL.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJarVoid>> JAR_VOID =
            BLOCK_ENTITIES.register(
                    "jar_void",
                    () -> new BlockEntityType<>(BlockEntityJarVoid::new, Set.of(TCBlocks.JAR_VOID.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTube>> TUBE =
            BLOCK_ENTITIES.register(
                    "tube",
                    () -> new BlockEntityType<>(BlockEntityTube::new, Set.of(TCBlocks.TUBE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeValve>> TUBE_VALVE =
            BLOCK_ENTITIES.register(
                    "tube_valve",
                    () -> new BlockEntityType<>(BlockEntityTubeValve::new, Set.of(TCBlocks.TUBE_VALVE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeRestrict>> TUBE_RESTRICT =
            BLOCK_ENTITIES.register(
                    "tube_restrict",
                    () -> new BlockEntityType<>(BlockEntityTubeRestrict::new, Set.of(TCBlocks.TUBE_RESTRICT.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeFilter>> TUBE_FILTER =
            BLOCK_ENTITIES.register(
                    "tube_filter",
                    () -> new BlockEntityType<>(BlockEntityTubeFilter::new, Set.of(TCBlocks.TUBE_FILTER.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeOneway>> TUBE_ONEWAY =
            BLOCK_ENTITIES.register(
                    "tube_oneway",
                    () -> new BlockEntityType<>(BlockEntityTubeOneway::new, Set.of(TCBlocks.TUBE_ONEWAY.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTubeBuffer>> TUBE_BUFFER =
            BLOCK_ENTITIES.register(
                    "tube_buffer",
                    () -> new BlockEntityType<>(BlockEntityTubeBuffer::new, Set.of(TCBlocks.TUBE_BUFFER.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityNitor>> NITOR =
            BLOCK_ENTITIES.register(
                    "nitor",
                    () -> new BlockEntityType<>(BlockEntityNitor::new,
                            TCBlocks.NITORS.values().stream().map(b -> (Block) b.get()).collect(Collectors.toSet()))
            );

    private TCBlockEntities() {}

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
