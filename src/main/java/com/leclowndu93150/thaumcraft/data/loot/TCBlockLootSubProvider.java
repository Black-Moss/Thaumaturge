package com.leclowndu93150.thaumcraft.data.loot;

import com.leclowndu93150.thaumcraft.data.fragments.*;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

public final class TCBlockLootSubProvider extends BlockLootSubProvider {
    private LootTable.Builder bannerTable(ItemLike item) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(item, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(item)
                                .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)))));
    }

    private final HolderLookup.Provider lookupProvider;

    public TCBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
        this.lookupProvider = lookupProvider;
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TCBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.value())
                .toList();
    }

    @Override
    protected void generate() {
        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TCBlocks.CANDLES.get(dye).get());
            add(TCBlocks.BANNERS.get(dye).get(), bannerTable(TCItems.BANNERS.get(dye).get()));
            add(TCBlocks.WALL_BANNERS.get(dye).get(), bannerTable(TCItems.BANNERS.get(dye).get()));
        }
        add(TCBlocks.BANNER_CRIMSON_CULT.get(), bannerTable(TCItems.BANNER_CRIMSON_CULT.get()));
        add(TCBlocks.WALL_BANNER_CRIMSON_CULT.get(), bannerTable(TCItems.BANNER_CRIMSON_CULT.get()));
        generateResources();

        dropSelf(TCBlocks.RESEARCH_TABLE.get());
        dropSelf(TCBlocks.ARCANE_WORKBENCH.get());
        dropSelf(TCBlocks.ARCANE_WORKBENCH_CHARGER.get());
        dropSelf(TCBlocks.CRUCIBLE.get());
        dropSelf(TCBlocks.ALEMBIC.get());
        dropSelf(TCBlocks.SMELTER_BASIC.get());
        dropSelf(TCBlocks.SMELTER_THAUMIUM.get());
        dropSelf(TCBlocks.SMELTER_VOID.get());
        add(TCBlocks.JAR_NORMAL.get(), jarLootTable(TCBlocks.JAR_NORMAL.get()));
        add(TCBlocks.JAR_VOID.get(), jarLootTable(TCBlocks.JAR_VOID.get()));
        dropSelf(TCBlocks.TUBE.get());
        dropSelf(TCBlocks.TUBE_VALVE.get());
        dropSelf(TCBlocks.TUBE_RESTRICT.get());
        dropSelf(TCBlocks.TUBE_FILTER.get());
        dropSelf(TCBlocks.TUBE_ONEWAY.get());
        dropSelf(TCBlocks.TUBE_BUFFER.get());

        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TCBlocks.NITORS.get(dye).get());
        }
        add(TCBlocks.CRYSTAL_AER.get(), TCBlocksBCrystalsLoot.crystalAer());
        add(TCBlocks.CRYSTAL_IGNIS.get(), TCBlocksBCrystalsLoot.crystalIgnis());
        add(TCBlocks.CRYSTAL_AQUA.get(), TCBlocksBCrystalsLoot.crystalAqua());
        add(TCBlocks.CRYSTAL_TERRA.get(), TCBlocksBCrystalsLoot.crystalTerra());
        add(TCBlocks.CRYSTAL_ORDO.get(), TCBlocksBCrystalsLoot.crystalOrdo());
        add(TCBlocks.CRYSTAL_PERDITIO.get(), TCBlocksBCrystalsLoot.crystalPerditio());
        add(TCBlocks.CRYSTAL_VITIUM.get(), TCBlocksBCrystalsLoot.crystalVitium());
        TCBlocksCStoneLoot.register(this::dropSelf, this::add, noDrop());
        TCBlocksDTreesLoot.register(this::dropSelf, this::add, this::createLeavesDrops);
        TCBlocksEPlantsLoot.register(this::dropSelf);
        add(TCBlocks.GRASS_AMBIENT.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropSelf(TCBlocks.TAINT_ROCK.get());
        dropSelf(TCBlocks.TAINT_SOIL.get());
        dropSelf(TCBlocks.TAINT_CRUST.get());
        dropSelf(TCBlocks.TAINT_GEYSER.get());
        dropSelf(TCBlocks.TAINT_LOG.get());
        dropSelf(TCBlocks.TAINT_FEATURE.get());
        add(TCBlocks.TAINT_FIBRE.get(), noDrop());
    }

    private LootTable.Builder createLeavesDrops(Block leaves, Block sapling) {
        return createLeavesDrops(leaves, sapling, NORMAL_LEAVES_SAPLING_CHANCES);
    }

    private LootTable.Builder jarLootTable(Block block) {
        return LootTable.lootTable().withPool(
                applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(block)
                                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                                .include(TCDataComponents.ESSENTIA_CONTENTS.get())
                                                .include(TCDataComponents.ASPECT_FILTER.get())))
                )
        );
    }

    private void generateResources(){
        add(TCBlocks.ORE_AMBER.get(),b->createOreDrop(b, TCItems.AMBER.get()));
        dropSelf(TCBlocks.ORE_CINNABAR.get());
        add(TCBlocks.ORE_QUARTZ.get(),b->createOreDrop(b, Items.QUARTZ));

        dropSelf(TCBlocks.METAL_BRASS_BLOCK.get());
        dropSelf(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        dropSelf(TCBlocks.METAL_VOID_BLOCK.get());
        dropSelf(TCBlocks.AMBER_BLOCK.get());
    }
}
