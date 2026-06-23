package com.leclowndu93150.thaumcraft.registry.items;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.item.ScribingToolsItem;
import com.leclowndu93150.thaumcraft.content.item.ThaumometerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCItemsGTools {
    public static final int SCRIBING_TOOLS_DURABILITY = 100;

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TCIds.MODID);

    public static final DeferredItem<ThaumometerItem> THAUMOMETER = ITEMS.registerItem(
            "thaumometer",
            ThaumometerItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<ScribingToolsItem> SCRIBING_TOOLS = ITEMS.registerItem(
            "scribing_tools",
            ScribingToolsItem::new,
            props -> props.stacksTo(1).durability(SCRIBING_TOOLS_DURABILITY)
    );

    private TCItemsGTools() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
