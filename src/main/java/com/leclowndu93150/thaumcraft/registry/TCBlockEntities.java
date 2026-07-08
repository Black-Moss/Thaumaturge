package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.casters.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumcraft.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityArcaneEar;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityDioptra;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityHungryChest;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityLampArcane;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityLampFertility;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityLampGrowth;
import com.leclowndu93150.thaumcraft.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJarBrain;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJarVoid;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumcraft.content.spa.BlockEntitySpa;
import com.leclowndu93150.thaumcraft.content.focus.BlockEntityHole;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTube;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeFilter;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeOneway;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeRestrict;
import com.leclowndu93150.thaumcraft.content.essentia.tube.BlockEntityTubeValve;
import com.leclowndu93150.thaumcraft.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumcraft.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumcraft.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumcraft.content.aura.BlockEntityRechargePedestal;
import com.leclowndu93150.thaumcraft.content.device.BlockEntityLevitator;
import com.leclowndu93150.thaumcraft.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumcraft.content.misc.nitor.BlockEntityNitor;
import com.leclowndu93150.thaumcraft.content.research.table.BlockEntityResearchTable;
import java.util.Set;
import java.util.stream.Collectors;

import com.leclowndu93150.thaumcraft.content.workbench.BlockEntityArcaneWorkbench;
import com.leclowndu93150.thaumcraft.content.decor.banner.BlockEntityBanner;
import java.util.HashSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TCIds.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityBanner>> BANNER =
            BLOCK_ENTITIES.register(
                    "banner",
                    () -> new BlockEntityType<>(BlockEntityBanner::new, bannerBlocks())
            );

    private static Set<Block> bannerBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (DyeColor dye : DyeColor.values()) {
            blocks.add(TCBlocks.BANNERS.get(dye).get());
            blocks.add(TCBlocks.WALL_BANNERS.get(dye).get());
        }
        blocks.add(TCBlocks.BANNER_CRIMSON_CULT.get());
        blocks.add(TCBlocks.WALL_BANNER_CRIMSON_CULT.get());
        return blocks;
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityFocalManipulator>> FOCAL_MANIPULATOR =
            BLOCK_ENTITIES.register(
                    "focal_manipulator",
                    () -> new BlockEntityType<>(BlockEntityFocalManipulator::new, Set.of(TCBlocks.FOCAL_MANIPULATOR.get()))
            );

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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySpa>> SPA =
            BLOCK_ENTITIES.register(
                    "spa",
                    () -> new BlockEntityType<>(BlockEntitySpa::new, Set.of(TCBlocks.SPA.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySmelter>> SMELTER =
            BLOCK_ENTITIES.register(
                    "smelter",
                    () -> new BlockEntityType<>(BlockEntitySmelter::new, Set.of(TCBlocks.SMELTER_BASIC.get(),TCBlocks.SMELTER_THAUMIUM.get(),TCBlocks.SMELTER_VOID.get()))
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityJarBrain>> JAR_BRAIN =
            BLOCK_ENTITIES.register(
                    "jar_brain",
                    () -> new BlockEntityType<>(BlockEntityJarBrain::new, Set.of(TCBlocks.JAR_BRAIN.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityArcaneEar>> ARCANE_EAR =
            BLOCK_ENTITIES.register(
                    "arcane_ear",
                    () -> new BlockEntityType<>(BlockEntityArcaneEar::new,
                            Set.of(TCBlocks.ARCANE_EAR.get(), TCBlocks.ARCANE_EAR_TOGGLE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLampArcane>> LAMP_ARCANE =
            BLOCK_ENTITIES.register(
                    "lamp_arcane",
                    () -> new BlockEntityType<>(BlockEntityLampArcane::new, Set.of(TCBlocks.LAMP_ARCANE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLampGrowth>> LAMP_GROWTH =
            BLOCK_ENTITIES.register(
                    "lamp_growth",
                    () -> new BlockEntityType<>(BlockEntityLampGrowth::new, Set.of(TCBlocks.LAMP_GROWTH.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLampFertility>> LAMP_FERTILITY =
            BLOCK_ENTITIES.register(
                    "lamp_fertility",
                    () -> new BlockEntityType<>(BlockEntityLampFertility::new, Set.of(TCBlocks.LAMP_FERTILITY.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityCentrifuge>> CENTRIFUGE =
            BLOCK_ENTITIES.register(
                    "centrifuge",
                    () -> new BlockEntityType<>(BlockEntityCentrifuge::new, Set.of(TCBlocks.CENTRIFUGE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityHungryChest>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register(
                    "hungry_chest",
                    () -> new BlockEntityType<>(BlockEntityHungryChest::new, Set.of(TCBlocks.HUNGRY_CHEST.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityDioptra>> DIOPTRA =
            BLOCK_ENTITIES.register(
                    "dioptra",
                    () -> new BlockEntityType<>(BlockEntityDioptra::new, Set.of(TCBlocks.DIOPTRA.get()))
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityInfernalFurnace>> INFERNAL_FURNACE =
            BLOCK_ENTITIES.register(
                    "infernal_furnace",
                    () -> new BlockEntityType<>(BlockEntityInfernalFurnace::new, Set.of(TCBlocks.INFERNAL_FURNACE.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityInfusionMatrix>> INFUSION_MATRIX =
            BLOCK_ENTITIES.register(
                    "infusion_matrix",
                    () -> new BlockEntityType<>(BlockEntityInfusionMatrix::new, Set.of(TCBlocks.INFUSION_MATRIX.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityPedestal>> PEDESTAL =
            BLOCK_ENTITIES.register(
                    "pedestal",
                    () -> new BlockEntityType<>(BlockEntityPedestal::new,
                            Set.of(TCBlocks.PEDESTAL_ARCANE.get(), TCBlocks.PEDESTAL_ANCIENT.get(), TCBlocks.PEDESTAL_ELDRITCH.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityRechargePedestal>> RECHARGE_PEDESTAL =
            BLOCK_ENTITIES.register(
                    "recharge_pedestal",
                    () -> new BlockEntityType<>(BlockEntityRechargePedestal::new, Set.of(TCBlocks.RECHARGE_PEDESTAL.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityLevitator>> LEVITATOR =
            BLOCK_ENTITIES.register(
                    "levitator",
                    () -> new BlockEntityType<>(BlockEntityLevitator::new, Set.of(TCBlocks.LEVITATOR.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityGolemBuilder>> GOLEM_BUILDER =
            BLOCK_ENTITIES.register(
                    "golem_builder",
                    () -> new BlockEntityType<>(BlockEntityGolemBuilder::new, Set.of(TCBlocks.GOLEM_BUILDER.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityHole>> HOLE =
            BLOCK_ENTITIES.register(
                    "hole",
                    () -> new BlockEntityType<>(BlockEntityHole::new, Set.of(TCBlocks.HOLE.get()))
            );

    private TCBlockEntities() {}

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
