package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsAOres;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsBCrystals;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsCStone;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsDTrees;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsGTools;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsHContainers;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsMAuraHud;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TCIds.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THAUMCRAFT = CREATIVE_MODE_TABS.register(
            "thaumcraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumcraft"))
                    .icon(() -> new ItemStack(Items.BOOK))
                    .displayItems((parameters, output) -> {
                        output.accept(TCItems.THAUMONOMICON.get());
                        output.accept(TCItems.SALIS_MUNDUS.get());
                        output.accept(TCItemsGTools.THAUMOMETER.get());
                        output.accept(TCItemsGTools.SCRIBING_TOOLS.get());
                        output.accept(TCItemsMAuraHud.GOGGLES_REVEALING.get());
                        output.accept(TCItems.LABEL.get());
                        output.accept(TCItems.RESEARCH_TABLE.get());
                        output.accept(TCItems.JAR_NORMAL.get());
                        output.accept(TCItems.JAR_VOID.get());
                        output.accept(TCItems.JAR_BRACE.get());
                        output.accept(TCItems.TUBE.get());
                        output.accept(TCItems.TUBE_VALVE.get());
                        output.accept(TCItems.TUBE_RESTRICT.get());
                        output.accept(TCItems.TUBE_FILTER.get());
                        output.accept(TCItems.TUBE_ONEWAY.get());
                        output.accept(TCItems.TUBE_BUFFER.get());
                        output.accept(TCItemsAOres.ORE_AMBER.get());
                        output.accept(TCItemsAOres.ORE_CINNABAR.get());
                        output.accept(TCItemsAOres.ORE_QUARTZ.get());
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItemsAOres.NITORS.get(dye).get());
                        }
                        output.accept(TCItemsAOres.AMBER.get());
                        output.accept(TCItemsAOres.CINNABAR.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_AER.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_IGNIS.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_AQUA.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_TERRA.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_ORDO.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_PERDITIO.get());
                        output.accept(TCItemsBCrystals.CRYSTAL_VITIUM.get());
                        output.accept(TCItemsCStone.STONE_ARCANE.get());
                        output.accept(TCItemsCStone.STONE_ARCANE_BRICK.get());
                        output.accept(TCItemsCStone.STAIRS_ARCANE.get());
                        output.accept(TCItemsCStone.STAIRS_ARCANE_BRICK.get());
                        output.accept(TCItemsCStone.STONE_ANCIENT.get());
                        output.accept(TCItemsCStone.STONE_ANCIENT_TILE.get());
                        output.accept(TCItemsCStone.STONE_ANCIENT_GLYPHED.get());
                        output.accept(TCItemsCStone.STAIRS_ANCIENT.get());
                        output.accept(TCItemsCStone.STONE_ELDRITCH_TILE.get());
                        output.accept(TCItemsCStone.STONE_POROUS.get());
                        output.accept(TCItemsDTrees.SAPLING_GREATWOOD.get());
                        output.accept(TCItemsDTrees.SAPLING_SILVERWOOD.get());
                        output.accept(TCItemsDTrees.LOG_GREATWOOD.get());
                        output.accept(TCItemsDTrees.LOG_SILVERWOOD.get());
                        output.accept(TCItemsDTrees.LEAVES_GREATWOOD.get());
                        output.accept(TCItemsDTrees.LEAVES_SILVERWOOD.get());
                        output.accept(TCItemsDTrees.PLANK_GREATWOOD.get());
                        output.accept(TCItemsDTrees.PLANK_SILVERWOOD.get());
                        output.accept(TCItemsHContainers.PHIAL.get());
                        output.accept(TCItemsHContainers.PRIMORDIAL_PEARL.get());
                    })
                    .build()
    );

    private TCCreativeTabs() {}

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
