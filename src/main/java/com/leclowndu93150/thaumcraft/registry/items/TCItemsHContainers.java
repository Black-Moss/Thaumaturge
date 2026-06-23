package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import com.leclowndu93150.thaumcraft.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TCItemsHContainers {
    public static final DeferredItem<PhialItem> PHIAL = TCItems.ITEMS.registerItem(
            "phial",
            PhialItem::new
    );

    public static final DeferredItem<PrimordialPearlItem> PRIMORDIAL_PEARL = TCItems.ITEMS.registerItem(
            "primordial_pearl",
            PrimordialPearlItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON).durability(PrimordialPearlItem.MAX_DAMAGE)
    );

    private TCItemsHContainers() {}

    public static void touch() {}

    public static void register(IEventBus modBus) {}
}
