package com.leclowndu93150.thaumcraft.data.loot;

import com.leclowndu93150.thaumcraft.data.fragments.TCBlocksAOresLoot;
import com.leclowndu93150.thaumcraft.data.fragments.TCBlocksBCrystalsLoot;
import com.leclowndu93150.thaumcraft.data.fragments.TCBlocksCStoneLoot;
import com.leclowndu93150.thaumcraft.data.fragments.TCBlocksDTreesLoot;
import com.leclowndu93150.thaumcraft.data.fragments.TCBlocksEPlantsLoot;
import com.leclowndu93150.thaumcraft.data.fragments.TCBlocksFMetalsLoot;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksAOres;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksBCrystals;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class TCBlockLootSubProvider extends BlockLootSubProvider {
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
        dropSelf(TCBlocks.RESEARCH_TABLE.get());
        add(TCBlocks.JAR_NORMAL.get(), jarLootTable(TCBlocks.JAR_NORMAL.get()));
        add(TCBlocks.JAR_VOID.get(), jarLootTable(TCBlocks.JAR_VOID.get()));
        dropSelf(TCBlocks.TUBE.get());
        dropSelf(TCBlocks.TUBE_VALVE.get());
        dropSelf(TCBlocks.TUBE_RESTRICT.get());
        dropSelf(TCBlocks.TUBE_FILTER.get());
        dropSelf(TCBlocks.TUBE_ONEWAY.get());
        dropSelf(TCBlocks.TUBE_BUFFER.get());
        add(TCBlocksAOres.ORE_AMBER.get(), TCBlocksAOresLoot.amber(lookupProvider));
        add(TCBlocksAOres.ORE_CINNABAR.get(), TCBlocksAOresLoot.cinnabar(lookupProvider));
        add(TCBlocksAOres.ORE_QUARTZ.get(), TCBlocksAOresLoot.quartz(lookupProvider));
        for (DyeColor dye : DyeColor.values()) {
            dropSelf(TCBlocksAOres.NITORS.get(dye).get());
        }
        add(TCBlocksBCrystals.CRYSTAL_AER.get(), TCBlocksBCrystalsLoot.crystalAer());
        add(TCBlocksBCrystals.CRYSTAL_IGNIS.get(), TCBlocksBCrystalsLoot.crystalIgnis());
        add(TCBlocksBCrystals.CRYSTAL_AQUA.get(), TCBlocksBCrystalsLoot.crystalAqua());
        add(TCBlocksBCrystals.CRYSTAL_TERRA.get(), TCBlocksBCrystalsLoot.crystalTerra());
        add(TCBlocksBCrystals.CRYSTAL_ORDO.get(), TCBlocksBCrystalsLoot.crystalOrdo());
        add(TCBlocksBCrystals.CRYSTAL_PERDITIO.get(), TCBlocksBCrystalsLoot.crystalPerditio());
        add(TCBlocksBCrystals.CRYSTAL_VITIUM.get(), TCBlocksBCrystalsLoot.crystalVitium());
        TCBlocksCStoneLoot.register(this::dropSelf, this::add, noDrop());
        TCBlocksFMetalsLoot.register(this::dropSelf);
        TCBlocksDTreesLoot.register(this::dropSelf, this::add, this::createLeavesDrops);
        TCBlocksEPlantsLoot.register(this::dropSelf);
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

    public void dropSelfPublic(Block block) {
        dropSelf(block);
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
}
