package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class TCBlocksBCrystalsLoot {
    private TCBlocksBCrystalsLoot() {}

    public static LootTable.Builder dropSelf(Block block) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block))
        );
    }

    public static LootTable.Builder crystalAer() {
        return dropSelf(TCBlocks.CRYSTAL_AER.get());
    }

    public static LootTable.Builder crystalIgnis() {
        return dropSelf(TCBlocks.CRYSTAL_IGNIS.get());
    }

    public static LootTable.Builder crystalAqua() {
        return dropSelf(TCBlocks.CRYSTAL_AQUA.get());
    }

    public static LootTable.Builder crystalTerra() {
        return dropSelf(TCBlocks.CRYSTAL_TERRA.get());
    }

    public static LootTable.Builder crystalOrdo() {
        return dropSelf(TCBlocks.CRYSTAL_ORDO.get());
    }

    public static LootTable.Builder crystalPerditio() {
        return dropSelf(TCBlocks.CRYSTAL_PERDITIO.get());
    }

    public static LootTable.Builder crystalVitium() {
        return dropSelf(TCBlocks.CRYSTAL_VITIUM.get());
    }
}
