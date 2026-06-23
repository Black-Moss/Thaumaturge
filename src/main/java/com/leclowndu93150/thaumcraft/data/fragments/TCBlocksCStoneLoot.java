package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksCStone;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

public final class TCBlocksCStoneLoot {
    private TCBlocksCStoneLoot() {}

    public static void register(Consumer<Block> dropSelf, BiConsumer<Block, LootTable.Builder> add, LootTable.Builder noDrop) {
        dropSelf.accept(TCBlocksCStone.STONE_ARCANE.get());
        dropSelf.accept(TCBlocksCStone.STONE_ARCANE_BRICK.get());
        dropSelf.accept(TCBlocksCStone.STONE_ANCIENT.get());
        dropSelf.accept(TCBlocksCStone.STONE_ANCIENT_TILE.get());
        dropSelf.accept(TCBlocksCStone.STONE_ANCIENT_GLYPHED.get());
        dropSelf.accept(TCBlocksCStone.STONE_ELDRITCH_TILE.get());
        dropSelf.accept(TCBlocksCStone.STONE_POROUS.get());
        dropSelf.accept(TCBlocksCStone.STAIRS_ARCANE.get());
        dropSelf.accept(TCBlocksCStone.STAIRS_ARCANE_BRICK.get());
        dropSelf.accept(TCBlocksCStone.STAIRS_ANCIENT.get());
        add.accept(TCBlocksCStone.STONE_ANCIENT_ROCK.get(), noDrop);
        add.accept(TCBlocksCStone.STONE_ANCIENT_DOORWAY.get(), noDrop);
    }
}
