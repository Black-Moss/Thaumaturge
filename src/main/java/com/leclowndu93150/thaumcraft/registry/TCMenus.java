package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.MenuSmelter;
import com.leclowndu93150.thaumcraft.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumcraft.content.workbench.MenuArcaneWorkbench;
import net.minecraft.core.registries.Registries;
import com.leclowndu93150.thaumcraft.content.casters.MenuFocalManipulator;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TCIds.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuFocalManipulator>> FOCAL_MANIPULATOR =
            MENUS.register("focal_manipulator", () -> IMenuTypeExtension.create(MenuFocalManipulator::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuResearchTable>> RESEARCH_TABLE =
            MENUS.register("research_table", () -> IMenuTypeExtension.create(MenuResearchTable::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuArcaneWorkbench>> ARCANE_WORKBENCH =
            MENUS.register("arcane_workbench", () -> IMenuTypeExtension.create(MenuArcaneWorkbench::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSmelter>> SMELTER =
            MENUS.register("smelter", () -> IMenuTypeExtension.create(MenuSmelter::new));

    private TCMenus() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
