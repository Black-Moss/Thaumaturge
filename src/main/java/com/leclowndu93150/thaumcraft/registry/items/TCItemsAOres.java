package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.blocks.TCBlocksAOres;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsAOres {
    public static final DeferredItem<BlockItem> ORE_AMBER = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksAOres.ORE_AMBER);
    public static final DeferredItem<BlockItem> ORE_CINNABAR = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksAOres.ORE_CINNABAR);
    public static final DeferredItem<BlockItem> ORE_QUARTZ = TCItems.ITEMS.registerSimpleBlockItem(TCBlocksAOres.ORE_QUARTZ);

    public static final Map<DyeColor, DeferredItem<BlockItem>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, TCItems.ITEMS.registerSimpleBlockItem(TCBlocksAOres.NITORS.get(dye)));
        }
    }

    public static final DeferredItem<Item> AMBER = TCItems.ITEMS.registerSimpleItem("amber");
    public static final DeferredItem<Item> CINNABAR = TCItems.ITEMS.registerSimpleItem("cinnabar");

    private TCItemsAOres() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
