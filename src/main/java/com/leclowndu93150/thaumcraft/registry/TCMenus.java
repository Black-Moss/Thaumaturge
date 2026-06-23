package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.research.table.MenuResearchTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TCIds.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuResearchTable>> RESEARCH_TABLE =
            MENUS.register("research_table", () -> IMenuTypeExtension.create(MenuResearchTable::new));

    private TCMenus() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
