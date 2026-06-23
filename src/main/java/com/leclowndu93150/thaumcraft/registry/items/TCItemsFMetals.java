package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksFMetals;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsFMetals {
    public static final DeferredItem<BlockItem> METAL_THAUMIUM_BLOCK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksFMetals.METAL_THAUMIUM_BLOCK);
    public static final DeferredItem<BlockItem> METAL_BRASS_BLOCK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksFMetals.METAL_BRASS_BLOCK);
    public static final DeferredItem<BlockItem> METAL_VOID_BLOCK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksFMetals.METAL_VOID_BLOCK);
    public static final DeferredItem<BlockItem> METAL_INFUSED_BLOCK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksFMetals.METAL_INFUSED_BLOCK);

    public static final DeferredItem<Item> INGOT_THAUMIUM = TCItems.ITEMS.registerSimpleItem("ingot_thaumium");
    public static final DeferredItem<Item> INGOT_BRASS = TCItems.ITEMS.registerSimpleItem("ingot_brass");
    public static final DeferredItem<Item> INGOT_VOID = TCItems.ITEMS.registerSimpleItem("ingot_void");
    public static final DeferredItem<Item> INGOT_INFUSED = TCItems.ITEMS.registerSimpleItem("ingot_infused");

    public static final DeferredItem<Item> NUGGET_THAUMIUM = TCItems.ITEMS.registerSimpleItem("nugget_thaumium");
    public static final DeferredItem<Item> NUGGET_BRASS = TCItems.ITEMS.registerSimpleItem("nugget_brass");
    public static final DeferredItem<Item> NUGGET_VOID = TCItems.ITEMS.registerSimpleItem("nugget_void");

    public static final DeferredItem<Item> CLUSTER_IRON = TCItems.ITEMS.registerSimpleItem("cluster_iron");
    public static final DeferredItem<Item> CLUSTER_GOLD = TCItems.ITEMS.registerSimpleItem("cluster_gold");
    public static final DeferredItem<Item> CLUSTER_COPPER = TCItems.ITEMS.registerSimpleItem("cluster_copper");
    public static final DeferredItem<Item> CLUSTER_SILVER = TCItems.ITEMS.registerSimpleItem("cluster_silver");
    public static final DeferredItem<Item> CLUSTER_LEAD = TCItems.ITEMS.registerSimpleItem("cluster_lead");
    public static final DeferredItem<Item> CLUSTER_TIN = TCItems.ITEMS.registerSimpleItem("cluster_tin");
    public static final DeferredItem<Item> CLUSTER_THAUMIUM = TCItems.ITEMS.registerSimpleItem("cluster_thaumium");
    public static final DeferredItem<Item> CLUSTER_BRASS = TCItems.ITEMS.registerSimpleItem("cluster_brass");

    private TCItemsFMetals() {}

    public static void touch() {}
}
