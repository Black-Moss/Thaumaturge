package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;

public final class TCBlocksFMetalsLoot {
    private TCBlocksFMetalsLoot() {}

    public static void register(Consumer<Block> dropSelf) {
        dropSelf.accept(TCBlocks.METAL_THAUMIUM_BLOCK.get());
        dropSelf.accept(TCBlocks.METAL_BRASS_BLOCK.get());
        dropSelf.accept(TCBlocks.METAL_VOID_BLOCK.get());
        dropSelf.accept(TCBlocks.METAL_INFUSED_BLOCK.get());
    }
}
