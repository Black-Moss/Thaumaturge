package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.content.item.CelestialBody;
import com.leclowndu93150.thaumcraft.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;

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
                        output.accept(TCItems.THAUMOMETER.get());
                        output.accept(TCItems.SCRIBING_TOOLS.get());
                        output.accept(TCItems.VIS_RESONATOR.get());
                        for (CelestialBody body : CelestialBody.values()) {
                            output.accept(CelestialNotesItem.stackOf(body));
                        }
                        output.accept(TCItems.GOGGLES_REVEALING.get());
                        output.accept(TCItems.LABEL.get());
                        output.accept(TCItems.RESEARCH_TABLE.get());
                        output.accept(TCItems.ARCANE_WORKBENCH.get());
                        output.accept(TCItems.CRUCIBLE.get());
                        output.accept(TCItems.ARCANE_WORKBENCH_CHARGER.get());
                        output.accept(TCItems.ALEMBIC.get());
                        output.accept(TCItems.SMELTER_BASIC.get());
                        output.accept(TCItems.SMELTER_THAUMIUM.get());
                        output.accept(TCItems.SMELTER_VOID.get());
                        output.accept(TCItems.JAR_NORMAL.get());
                        output.accept(TCItems.JAR_VOID.get());
                        output.accept(TCItems.JAR_BRACE.get());
                        output.accept(TCItems.TUBE.get());
                        output.accept(TCItems.TUBE_VALVE.get());
                        output.accept(TCItems.TUBE_RESTRICT.get());
                        output.accept(TCItems.TUBE_FILTER.get());
                        output.accept(TCItems.TUBE_ONEWAY.get());
                        output.accept(TCItems.TUBE_BUFFER.get());
                        output.accept(TCItems.ORE_AMBER.get());
                        output.accept(TCItems.ORE_CINNABAR.get());
                        output.accept(TCItems.ORE_QUARTZ.get());
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItems.NITORS.get(dye).get());
                        }
                        output.accept(TCItems.TALLOW.get());
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItems.BANNERS.get(dye).get());
                        }
                        output.accept(TCItems.BANNER_CRIMSON_CULT.get());
                        for (DyeColor dye : DyeColor.values()) {
                            output.accept(TCItems.CANDLES.get(dye).get());
                        }
                        output.accept(TCItems.AMBER.get());
                        output.accept(TCItems.CINNABAR.get());
                        output.accept(TCItems.CRYSTAL_AER.get());
                        output.accept(TCItems.CRYSTAL_IGNIS.get());
                        output.accept(TCItems.CRYSTAL_AQUA.get());
                        output.accept(TCItems.CRYSTAL_TERRA.get());
                        output.accept(TCItems.CRYSTAL_ORDO.get());
                        output.accept(TCItems.CRYSTAL_PERDITIO.get());
                        output.accept(TCItems.CRYSTAL_VITIUM.get());
                        output.accept(TCItems.STONE_ARCANE.get());
                        output.accept(TCItems.STONE_ARCANE_BRICK.get());
                        output.accept(TCItems.STAIRS_ARCANE.get());
                        output.accept(TCItems.STAIRS_ARCANE_BRICK.get());
                        output.accept(TCItems.STONE_ANCIENT.get());
                        output.accept(TCItems.STONE_ANCIENT_TILE.get());
                        output.accept(TCItems.STONE_ANCIENT_GLYPHED.get());
                        output.accept(TCItems.STAIRS_ANCIENT.get());
                        output.accept(TCItems.STONE_ELDRITCH_TILE.get());
                        output.accept(TCItems.STONE_POROUS.get());
                        output.accept(TCItems.SAPLING_GREATWOOD.get());
                        output.accept(TCItems.SAPLING_SILVERWOOD.get());
                        output.accept(TCItems.LOG_GREATWOOD.get());
                        output.accept(TCItems.LOG_SILVERWOOD.get());
                        output.accept(TCItems.LEAVES_GREATWOOD.get());
                        output.accept(TCItems.LEAVES_SILVERWOOD.get());
                        output.accept(TCItems.PLANK_GREATWOOD.get());
                        output.accept(TCItems.PLANK_SILVERWOOD.get());
                        output.accept(TCItems.PLANT_SHIMMERLEAF.get());
                        output.accept(TCItems.PLANT_CINDERPEARL.get());
                        output.accept(TCItems.PLANT_VISHROOM.get());
                        output.accept(TCItems.GRASS_AMBIENT.get());
                        HolderLookup.RegistryLookup<IAspect> aspectRegistry = parameters.holders().lookupOrThrow(IAspect.REGISTRY_KEY);
                        for (Holder<IAspect> aspect : aspectRegistry.listElements().sorted(Comparator.comparing(h->!h.value().isPrimal())).toList()){
                            output.accept(EssentiaCrystalFactory.of(aspect));
                        }
                        output.accept(TCItems.PHIAL.get());
                        for (Holder<IAspect> aspect : aspectRegistry.listElements().sorted(Comparator.comparing(h->!h.value().isPrimal())).toList()){
                            output.accept(PhialItem.makeFilled(aspect));
                        }
                        output.accept(TCItems.PRIMORDIAL_PEARL.get());
                        output.accept(TCItems.BRAIN.get());
                        output.accept(TCItems.WISP_SPAWN_EGG.get());
                        output.accept(TCItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.FIREBAT_SPAWN_EGG.get());
                        output.accept(TCItems.MIND_SPIDER_SPAWN_EGG.get());
                        output.accept(TCItems.THAUMIC_SLIME_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_CRAWLER_SPAWN_EGG.get());
                        output.accept(TCItems.TAINTACLE_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SWARM_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SEED_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get());
                    })
                    .build()
    );

    private TCCreativeTabs() {}

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
