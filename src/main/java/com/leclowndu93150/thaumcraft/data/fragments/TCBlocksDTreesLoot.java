package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksDTrees;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

public final class TCBlocksDTreesLoot {
    private TCBlocksDTreesLoot() {}

    public static void register(
            Consumer<Block> dropSelf,
            BiConsumer<Block, LootTable.Builder> add,
            LeavesLoot leavesLoot
    ) {
        dropSelf.accept(TCBlocksDTrees.SAPLING_GREATWOOD.get());
        dropSelf.accept(TCBlocksDTrees.SAPLING_SILVERWOOD.get());
        dropSelf.accept(TCBlocksDTrees.LOG_GREATWOOD.get());
        dropSelf.accept(TCBlocksDTrees.LOG_SILVERWOOD.get());
        dropSelf.accept(TCBlocksDTrees.PLANK_GREATWOOD.get());
        dropSelf.accept(TCBlocksDTrees.PLANK_SILVERWOOD.get());
        add.accept(
                TCBlocksDTrees.LEAVES_GREATWOOD.get(),
                leavesLoot.build(TCBlocksDTrees.LEAVES_GREATWOOD.get(), TCBlocksDTrees.SAPLING_GREATWOOD.get())
        );
        add.accept(
                TCBlocksDTrees.LEAVES_SILVERWOOD.get(),
                leavesLoot.build(TCBlocksDTrees.LEAVES_SILVERWOOD.get(), TCBlocksDTrees.SAPLING_SILVERWOOD.get())
        );
    }

    @FunctionalInterface
    public interface LeavesLoot {
        LootTable.Builder build(Block leaves, Block sapling);
    }
}
