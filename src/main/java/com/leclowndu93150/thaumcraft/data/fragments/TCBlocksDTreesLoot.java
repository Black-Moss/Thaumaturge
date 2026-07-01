package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
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
        dropSelf.accept(TCBlocks.SAPLING_GREATWOOD.get());
        dropSelf.accept(TCBlocks.SAPLING_SILVERWOOD.get());
        dropSelf.accept(TCBlocks.LOG_GREATWOOD.get());
        dropSelf.accept(TCBlocks.LOG_SILVERWOOD.get());
        dropSelf.accept(TCBlocks.PLANK_GREATWOOD.get());
        dropSelf.accept(TCBlocks.PLANK_SILVERWOOD.get());
        add.accept(
                TCBlocks.LEAVES_GREATWOOD.get(),
                leavesLoot.build(TCBlocks.LEAVES_GREATWOOD.get(), TCBlocks.SAPLING_GREATWOOD.get())
        );
        add.accept(
                TCBlocks.LEAVES_SILVERWOOD.get(),
                leavesLoot.build(TCBlocks.LEAVES_SILVERWOOD.get(), TCBlocks.SAPLING_SILVERWOOD.get())
        );
    }

    @FunctionalInterface
    public interface LeavesLoot {
        LootTable.Builder build(Block leaves, Block sapling);
    }
}
