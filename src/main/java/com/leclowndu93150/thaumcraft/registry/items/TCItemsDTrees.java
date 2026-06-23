package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksDTrees;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsDTrees {
    public static final DeferredItem<BlockItem> SAPLING_GREATWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.SAPLING_GREATWOOD);
    public static final DeferredItem<BlockItem> SAPLING_SILVERWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.SAPLING_SILVERWOOD);
    public static final DeferredItem<BlockItem> LOG_GREATWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.LOG_GREATWOOD);
    public static final DeferredItem<BlockItem> LOG_SILVERWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.LOG_SILVERWOOD);
    public static final DeferredItem<BlockItem> LEAVES_GREATWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.LEAVES_GREATWOOD);
    public static final DeferredItem<BlockItem> LEAVES_SILVERWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.LEAVES_SILVERWOOD);
    public static final DeferredItem<BlockItem> PLANK_GREATWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.PLANK_GREATWOOD);
    public static final DeferredItem<BlockItem> PLANK_SILVERWOOD = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksDTrees.PLANK_SILVERWOOD);

    private TCItemsDTrees() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
