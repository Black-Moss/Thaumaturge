package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksBCrystals;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsBCrystals {
    public static final DeferredItem<BlockItem> CRYSTAL_AER = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_AER);
    public static final DeferredItem<BlockItem> CRYSTAL_IGNIS = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_IGNIS);
    public static final DeferredItem<BlockItem> CRYSTAL_AQUA = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_AQUA);
    public static final DeferredItem<BlockItem> CRYSTAL_TERRA = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_TERRA);
    public static final DeferredItem<BlockItem> CRYSTAL_ORDO = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_ORDO);
    public static final DeferredItem<BlockItem> CRYSTAL_PERDITIO = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_PERDITIO);
    public static final DeferredItem<BlockItem> CRYSTAL_VITIUM = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksBCrystals.CRYSTAL_VITIUM);

    private TCItemsBCrystals() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
