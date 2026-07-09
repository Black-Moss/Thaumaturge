package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.essentia.smeltery.MenuSmelter;
import com.leclowndu93150.thaumcraft.content.golem.press.MenuGolemBuilder;
import com.leclowndu93150.thaumcraft.content.golem.seals.MenuSealBase;
import com.leclowndu93150.thaumcraft.content.pech.MenuPech;
import com.leclowndu93150.thaumcraft.content.spa.MenuSpa;
import com.leclowndu93150.thaumcraft.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumcraft.content.workbench.MenuArcaneWorkbench;
import net.minecraft.core.registries.Registries;
import com.leclowndu93150.thaumcraft.content.casters.MenuFocalManipulator;
import com.leclowndu93150.thaumcraft.content.casters.MenuFocusPouch;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import com.leclowndu93150.thaumcraft.content.device.MenuVoidSiphon;
import com.leclowndu93150.thaumcraft.content.essentia.thaumatorium.MenuThaumatorium;
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

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSpa>> SPA =
            MENUS.register("spa", () -> IMenuTypeExtension.create(MenuSpa::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSmelter>> SMELTER =
            MENUS.register("smelter", () -> IMenuTypeExtension.create(MenuSmelter::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuGolemBuilder>> GOLEM_BUILDER =
            MENUS.register("golem_builder", () -> IMenuTypeExtension.create(MenuGolemBuilder::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuThaumatorium>> THAUMATORIUM =
            MENUS.register("thaumatorium", () -> IMenuTypeExtension.create(MenuThaumatorium::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuVoidSiphon>> VOID_SIPHON =
            MENUS.register("void_siphon", () -> IMenuTypeExtension.create(MenuVoidSiphon::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSealBase>> SEAL =
            MENUS.register("seal", () -> IMenuTypeExtension.create(MenuSealBase::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuFocusPouch>> FOCUS_POUCH =
            MENUS.register("focus_pouch", () -> IMenuTypeExtension.create(MenuFocusPouch::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuPech>> PECH =
            MENUS.register("pech", () -> IMenuTypeExtension.create(MenuPech::new));

    private TCMenus() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
