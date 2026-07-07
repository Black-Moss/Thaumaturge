package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumcraft.content.equipment.InfusionEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.content.item.CelestialBody;
import com.leclowndu93150.thaumcraft.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;
import java.util.Optional;

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
                        output.accept(TCItems.CASTER_BASIC.get());
                        output.accept(TCItems.FOCUS_1.get());
                        output.accept(TCItems.FOCUS_2.get());
                        output.accept(TCItems.FOCUS_3.get());
                        output.accept(TCItems.LABEL.get());
                        output.accept(TCItems.RESEARCH_TABLE.get());
                        output.accept(TCItems.FOCAL_MANIPULATOR.get());
                        output.accept(TCItems.ARCANE_WORKBENCH.get());
                        output.accept(TCItems.CRUCIBLE.get());
                        output.accept(TCItems.ARCANE_WORKBENCH_CHARGER.get());
                        output.accept(TCItems.ALCHEMICAL_CONSTRUCT.get());
                        output.accept(TCItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
                        output.accept(TCItems.ALEMBIC.get());
                        output.accept(TCItems.SMELTER_BASIC.get());
                        output.accept(TCItems.SMELTER_THAUMIUM.get());
                        output.accept(TCItems.SMELTER_VOID.get());
                        output.accept(TCItems.SMELTER_AUX.get());
                        output.accept(TCItems.SMELTER_VENT.get());
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
                        output.accept(TCItems.AMBER_BLOCK);
                        output.accept(TCItems.METAL_BRASS_BLOCK);
                        output.accept(TCItems.METAL_THAUMIUM_BLOCK);
                        output.accept(TCItems.METAL_VOID_BLOCK);

                        output.accept(TCItems.QUICKSILVER.get());
                        output.accept(TCItems.AMBER.get());
                        output.accept(TCItems.INGOT_BRASS.get());
                        output.accept(TCItems.INGOT_THAUMIUM.get());
                        output.accept(TCItems.INGOT_VOID.get());

                        output.accept(TCItems.NUGGET_QUARTZ.get());
                        output.accept(TCItems.NUGGET_QUICKSILVER.get());
                        output.accept(TCItems.NUGGET_BRASS.get());
                        output.accept(TCItems.NUGGET_THAUMIUM.get());
                        output.accept(TCItems.NUGGET_VOID.get());

                        output.accept(TCItems.PLATE_IRON.get());
                        output.accept(TCItems.PLATE_BRASS.get());
                        output.accept(TCItems.PLATE_THAUMIUM.get());
                        output.accept(TCItems.PLATE_VOID.get());

                        output.accept(TCItems.FABRIC.get());
                        output.accept(TCItems.MIRRORED_GLASS.get());
                        output.accept(TCItems.FILTER.get());
                        output.accept(TCItems.MECHANISM_SIMPLE.get());
                        output.accept(TCItems.MECHANISM_COMPLEX.get());
                        output.accept(TCItems.MORPHIC_RESONATOR.get());
                        output.accept(TCItems.SPA.get());
                        output.accept(TCItems.BATH_SALTS.get());
                        output.accept(TCItems.SANITY_SOAP.get());
                        output.accept(TCItems.CHUNK_BEEF.get());
                        output.accept(TCItems.CHUNK_CHICKEN.get());
                        output.accept(TCItems.CHUNK_PORK.get());
                        output.accept(TCItems.CHUNK_FISH.get());
                        output.accept(TCItems.CHUNK_RABBIT.get());
                        output.accept(TCItems.CHUNK_MUTTON.get());
                        output.accept(TCItems.TRIPLE_MEAT_TREAT.get());

                        output.accept(TCItems.CLUSTER_IRON.get());
                        output.accept(TCItems.CLUSTER_GOLD.get());
                        output.accept(TCItems.CLUSTER_COPPER.get());
                        addIfTag(parameters,output,TCItemTags.INGOTS_TIN,TCItems.CLUSTER_TIN.get());
                        addIfTag(parameters,output,TCItemTags.INGOTS_SILVER,TCItems.CLUSTER_SILVER.get());
                        addIfTag(parameters,output,TCItemTags.INGOTS_LEAD,TCItems.CLUSTER_LEAD.get());
                        output.accept(TCItems.CLUSTER_CINNABAR.get());
                        output.accept(TCItems.CLUSTER_QUARTZ.get());

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
                        output.accept(TCItems.THAUMIUM_SWORD.get());
                        output.accept(TCItems.THAUMIUM_PICKAXE.get());
                        output.accept(TCItems.THAUMIUM_AXE.get());
                        output.accept(TCItems.THAUMIUM_SHOVEL.get());
                        output.accept(TCItems.THAUMIUM_HOE.get());
                        output.accept(TCItems.THAUMIUM_HELM.get());
                        output.accept(TCItems.THAUMIUM_CHEST.get());
                        output.accept(TCItems.THAUMIUM_LEGS.get());
                        output.accept(TCItems.THAUMIUM_BOOTS.get());
                        output.accept(TCItems.VOID_SWORD.get());
                        output.accept(TCItems.VOID_PICKAXE.get());
                        output.accept(TCItems.VOID_AXE.get());
                        output.accept(TCItems.VOID_SHOVEL.get());
                        output.accept(TCItems.VOID_HOE.get());
                        output.accept(TCItems.VOID_HELM.get());
                        output.accept(TCItems.VOID_CHEST.get());
                        output.accept(TCItems.VOID_LEGS.get());
                        output.accept(TCItems.VOID_BOOTS.get());
                        ItemStack elementalSword = new ItemStack(TCItems.ELEMENTAL_SWORD.get());
                        InfusionEnchantmentHelper.add(elementalSword, InfusionEnchantment.ARCING, 2);
                        output.accept(elementalSword);
                        ItemStack elementalPickaxe = new ItemStack(TCItems.ELEMENTAL_PICKAXE.get());
                        InfusionEnchantmentHelper.add(elementalPickaxe, InfusionEnchantment.REFINING, 1);
                        InfusionEnchantmentHelper.add(elementalPickaxe, InfusionEnchantment.SOUNDING, 2);
                        output.accept(elementalPickaxe);
                        ItemStack elementalAxe = new ItemStack(TCItems.ELEMENTAL_AXE.get());
                        InfusionEnchantmentHelper.add(elementalAxe, InfusionEnchantment.BURROWING, 1);
                        InfusionEnchantmentHelper.add(elementalAxe, InfusionEnchantment.COLLECTOR, 1);
                        output.accept(elementalAxe);
                        ItemStack elementalShovel = new ItemStack(TCItems.ELEMENTAL_SHOVEL.get());
                        InfusionEnchantmentHelper.add(elementalShovel, InfusionEnchantment.DESTRUCTIVE, 1);
                        output.accept(elementalShovel);
                        output.accept(TCItems.ELEMENTAL_HOE.get());
                        ItemStack primalCrusher = new ItemStack(TCItems.PRIMAL_CRUSHER.get());
                        InfusionEnchantmentHelper.add(primalCrusher, InfusionEnchantment.DESTRUCTIVE, 1);
                        InfusionEnchantmentHelper.add(primalCrusher, InfusionEnchantment.REFINING, 1);
                        output.accept(primalCrusher);
                        output.accept(TCItems.RECHARGE_PEDESTAL.get());
                        output.accept(TCItems.TRAVELLER_BOOTS.get());
                        output.accept(TCItems.CLOTH_CHEST.get());
                        output.accept(TCItems.CLOTH_LEGS.get());
                        output.accept(TCItems.CLOTH_BOOTS.get());
                        output.accept(TCItems.INFUSION_MATRIX.get());
                        output.accept(TCItems.PEDESTAL_ARCANE.get());
                        output.accept(TCItems.PEDESTAL_ANCIENT.get());
                        output.accept(TCItems.PEDESTAL_ELDRITCH.get());
                        output.accept(TCItems.PILLAR_ARCANE.get());
                        output.accept(TCItems.PILLAR_ANCIENT.get());
                        output.accept(TCItems.PILLAR_ELDRITCH.get());
                        output.accept(TCItems.VOID_SEED.get());
                        output.accept(TCItems.CAUSALITY_COLLAPSER.get());
                        output.accept(TCItems.PRIMORDIAL_PEARL.get());
                        output.accept(TCItems.BRAIN.get());
                        output.accept(TCItems.WISP_SPAWN_EGG.get());
                        output.accept(TCItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(TCItems.FIREBAT_SPAWN_EGG.get());
                        output.accept(TCItems.MIND_SPIDER_SPAWN_EGG.get());
                        output.accept(TCItems.THAUMIC_SLIME_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_ROCK.get());
                        output.accept(TCItems.TAINT_SOIL.get());
                        output.accept(TCItems.TAINT_CRUST.get());
                        output.accept(TCItems.TAINT_GEYSER.get());
                        output.accept(TCItems.TAINT_LOG.get());
                        output.accept(TCItems.TAINT_FEATURE.get());
                        output.accept(TCItems.TAINT_FIBRE.get());
                        output.accept(TCItems.TAINT_CRAWLER_SPAWN_EGG.get());
                        output.accept(TCItems.TAINTACLE_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SWARM_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SEED_SPAWN_EGG.get());
                        output.accept(TCItems.TAINT_SEED_PRIME_SPAWN_EGG.get());
                    })
                    .build()
    );

            private static void addIfTag(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output, TagKey<Item> tag, Item item) {
                Optional<HolderSet.Named<Item>> tagOpt = parameters.holders().get(tag);
                if (tagOpt.isPresent()){
                    if (tagOpt.get().stream().findAny().isPresent())
                        output.accept(item);
                }
            }

    private TCCreativeTabs() {}

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
