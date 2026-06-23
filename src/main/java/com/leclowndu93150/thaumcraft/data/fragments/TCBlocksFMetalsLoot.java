package com.leclowndu93150.thaumcraft.data.fragments;

import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksFMetals;
import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;

public final class TCBlocksFMetalsLoot {
    private TCBlocksFMetalsLoot() {}

    public static void register(Consumer<Block> dropSelf) {
        dropSelf.accept(TCBlocksFMetals.METAL_THAUMIUM_BLOCK.get());
        dropSelf.accept(TCBlocksFMetals.METAL_BRASS_BLOCK.get());
        dropSelf.accept(TCBlocksFMetals.METAL_VOID_BLOCK.get());
        dropSelf.accept(TCBlocksFMetals.METAL_INFUSED_BLOCK.get());
    }
}
