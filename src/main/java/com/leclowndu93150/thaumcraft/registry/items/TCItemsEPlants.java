package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksEPlants;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsEPlants {
    public static final DeferredItem<BlockItem> PLANT_SHIMMERLEAF = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksEPlants.PLANT_SHIMMERLEAF);
    public static final DeferredItem<BlockItem> PLANT_CINDERPEARL = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksEPlants.PLANT_CINDERPEARL);
    public static final DeferredItem<BlockItem> PLANT_VISHROOM = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksEPlants.PLANT_VISHROOM);

    private TCItemsEPlants() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
