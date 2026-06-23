package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksCStone;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsCStone {
    public static final DeferredItem<BlockItem> STONE_ARCANE = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ARCANE);
    public static final DeferredItem<BlockItem> STONE_ARCANE_BRICK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ANCIENT);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_TILE = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ANCIENT_TILE);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_ROCK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ANCIENT_ROCK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_GLYPHED = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ANCIENT_GLYPHED);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_DOORWAY = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ANCIENT_DOORWAY);
    public static final DeferredItem<BlockItem> STONE_ELDRITCH_TILE = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_ELDRITCH_TILE);
    public static final DeferredItem<BlockItem> STONE_POROUS = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STONE_POROUS);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STAIRS_ARCANE);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE_BRICK = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STAIRS_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STAIRS_ANCIENT = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksCStone.STAIRS_ANCIENT);

    private TCItemsCStone() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
