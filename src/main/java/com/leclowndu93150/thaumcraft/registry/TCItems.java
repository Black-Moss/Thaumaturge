package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.essentia.jar.JarBraceItem;
import com.leclowndu93150.thaumcraft.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumcraft.content.item.CausalityCollapserItem;
import com.leclowndu93150.thaumcraft.content.item.CelestialBody;
import com.leclowndu93150.thaumcraft.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumcraft.content.item.LabelItem;
import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import com.leclowndu93150.thaumcraft.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumcraft.content.item.SalisMundusItem;
import com.leclowndu93150.thaumcraft.content.item.ScribingToolsItem;
import com.leclowndu93150.thaumcraft.content.item.ThaumometerItem;
import com.leclowndu93150.thaumcraft.content.item.equipment.GogglesItem;
import com.leclowndu93150.thaumcraft.content.research.book.ThaumonomiconItem;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemBottleTaint;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemEssentiaCrystal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import com.leclowndu93150.thaumcraft.content.equipment.RobeArmorItem;
import com.leclowndu93150.thaumcraft.content.equipment.TCMaterials;
import com.leclowndu93150.thaumcraft.content.equipment.VoidGearItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TCIds.MODID);

    public static final DeferredItem<BlockItem> RESEARCH_TABLE = ITEMS.registerSimpleBlockItem(TCBlocks.RESEARCH_TABLE);

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH = ITEMS.registerSimpleBlockItem(TCBlocks.ARCANE_WORKBENCH);

    public static final DeferredItem<BlockItem> CRUCIBLE = ITEMS.registerSimpleBlockItem(TCBlocks.CRUCIBLE);

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH_CHARGER = ITEMS.registerSimpleBlockItem(TCBlocks.ARCANE_WORKBENCH_CHARGER);

    public static final DeferredItem<BlockItem> ALEMBIC = ITEMS.registerSimpleBlockItem(TCBlocks.ALEMBIC);

    public static final DeferredItem<BlockItem> SMELTER_BASIC = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_BASIC);

    public static final DeferredItem<BlockItem> SMELTER_THAUMIUM = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_THAUMIUM);

    public static final DeferredItem<BlockItem> SMELTER_VOID = ITEMS.registerSimpleBlockItem(TCBlocks.SMELTER_VOID);

    public static final DeferredItem<BlockItem> INFERNAL_FURNACE = ITEMS.registerSimpleBlockItem(TCBlocks.INFERNAL_FURNACE);

    public static final DeferredItem<BlockItem> JAR_NORMAL = registerSimpleBlockItem(TCBlocks.JAR_NORMAL, JarItem::new);

    public static final DeferredItem<BlockItem> JAR_VOID = registerSimpleBlockItem(TCBlocks.JAR_VOID,JarItem::new);

    public static final DeferredItem<BlockItem> TUBE = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE);

    public static final DeferredItem<BlockItem> TUBE_VALVE = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_VALVE);

    public static final DeferredItem<BlockItem> TUBE_RESTRICT = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_RESTRICT);

    public static final DeferredItem<BlockItem> TUBE_FILTER = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_FILTER);

    public static final DeferredItem<BlockItem> TUBE_ONEWAY = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_ONEWAY);

    public static final DeferredItem<BlockItem> TUBE_BUFFER = ITEMS.registerSimpleBlockItem(TCBlocks.TUBE_BUFFER);

    public static final DeferredItem<BlockItem> TAINT_ROCK = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_ROCK);
    public static final DeferredItem<BlockItem> TAINT_SOIL = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_SOIL);
    public static final DeferredItem<BlockItem> TAINT_CRUST = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_CRUST);
    public static final DeferredItem<BlockItem> TAINT_GEYSER = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_GEYSER);
    public static final DeferredItem<BlockItem> TAINT_LOG = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_LOG);
    public static final DeferredItem<BlockItem> TAINT_FEATURE = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_FEATURE);
    public static final DeferredItem<BlockItem> TAINT_FIBRE = ITEMS.registerSimpleBlockItem(TCBlocks.TAINT_FIBRE);

    public static final DeferredItem<JarBraceItem> JAR_BRACE = ITEMS.registerItem(
            "jar_brace",
            JarBraceItem::new
    );

    public static final DeferredItem<LabelItem> LABEL = ITEMS.registerItem(
            "label",
            LabelItem::new
    );

    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON = ITEMS.registerItem(
            "thaumonomicon",
            ThaumonomiconItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<SalisMundusItem> SALIS_MUNDUS = ITEMS.registerItem(
            "salis_mundus",
            SalisMundusItem::new,
            props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ItemEssentiaCrystal> ESSENTIA_CRYSTAL = ITEMS.registerItem(
            "essentia_crystal",
            ItemEssentiaCrystal::new);

    public static final DeferredItem<ItemBottleTaint> BOTTLE_TAINT = ITEMS.registerItem(
            "bottle_taint",
            ItemBottleTaint::new,
            props -> props.stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> VIS_RESONATOR = ITEMS.registerSimpleItem("vis_resonator");


    public static final DeferredItem<Item> THAUMIUM_SWORD = ITEMS.registerItem("thaumium_sword",
            Item::new, props -> props.sword(TCMaterials.TOOL_THAUMIUM, 3.0F, -2.4F));
    public static final DeferredItem<Item> THAUMIUM_PICKAXE = ITEMS.registerItem("thaumium_pickaxe",
            Item::new, props -> props.pickaxe(TCMaterials.TOOL_THAUMIUM, 1.0F, -2.8F));
    public static final DeferredItem<Item> THAUMIUM_AXE = ITEMS.registerItem("thaumium_axe",
            Item::new, props -> props.axe(TCMaterials.TOOL_THAUMIUM, 4.5F, -3.0F));
    public static final DeferredItem<Item> THAUMIUM_SHOVEL = ITEMS.registerItem("thaumium_shovel",
            Item::new, props -> props.shovel(TCMaterials.TOOL_THAUMIUM, 1.5F, -3.0F));
    public static final DeferredItem<Item> THAUMIUM_HOE = ITEMS.registerItem("thaumium_hoe",
            Item::new, props -> props.hoe(TCMaterials.TOOL_THAUMIUM, -3.0F, 0.0F));
    public static final DeferredItem<Item> THAUMIUM_HELM = ITEMS.registerItem("thaumium_helm",
            Item::new, props -> props.humanoidArmor(TCMaterials.ARMOR_THAUMIUM, ArmorType.HELMET));
    public static final DeferredItem<Item> THAUMIUM_CHEST = ITEMS.registerItem("thaumium_chest",
            Item::new, props -> props.humanoidArmor(TCMaterials.ARMOR_THAUMIUM, ArmorType.CHESTPLATE));
    public static final DeferredItem<Item> THAUMIUM_LEGS = ITEMS.registerItem("thaumium_legs",
            Item::new, props -> props.humanoidArmor(TCMaterials.ARMOR_THAUMIUM, ArmorType.LEGGINGS));
    public static final DeferredItem<Item> THAUMIUM_BOOTS = ITEMS.registerItem("thaumium_boots",
            Item::new, props -> props.humanoidArmor(TCMaterials.ARMOR_THAUMIUM, ArmorType.BOOTS));

    public static final DeferredItem<VoidGearItem> VOID_SWORD = ITEMS.registerItem("void_sword",
            VoidGearItem::new, props -> props.sword(TCMaterials.TOOL_VOID, 3.0F, -2.4F));
    public static final DeferredItem<VoidGearItem> VOID_PICKAXE = ITEMS.registerItem("void_pickaxe",
            VoidGearItem::new, props -> props.pickaxe(TCMaterials.TOOL_VOID, 1.0F, -2.8F));
    public static final DeferredItem<VoidGearItem> VOID_AXE = ITEMS.registerItem("void_axe",
            VoidGearItem::new, props -> props.axe(TCMaterials.TOOL_VOID, 4.0F, -3.0F));
    public static final DeferredItem<VoidGearItem> VOID_SHOVEL = ITEMS.registerItem("void_shovel",
            VoidGearItem::new, props -> props.shovel(TCMaterials.TOOL_VOID, 1.5F, -3.0F));
    public static final DeferredItem<VoidGearItem> VOID_HOE = ITEMS.registerItem("void_hoe",
            VoidGearItem::new, props -> props.hoe(TCMaterials.TOOL_VOID, -3.0F, 0.0F));
    public static final DeferredItem<VoidGearItem> VOID_HELM = ITEMS.registerItem("void_helm",
            VoidGearItem::new, props -> props.humanoidArmor(TCMaterials.ARMOR_VOID, ArmorType.HELMET));
    public static final DeferredItem<VoidGearItem> VOID_CHEST = ITEMS.registerItem("void_chest",
            VoidGearItem::new, props -> props.humanoidArmor(TCMaterials.ARMOR_VOID, ArmorType.CHESTPLATE));
    public static final DeferredItem<VoidGearItem> VOID_LEGS = ITEMS.registerItem("void_legs",
            VoidGearItem::new, props -> props.humanoidArmor(TCMaterials.ARMOR_VOID, ArmorType.LEGGINGS));
    public static final DeferredItem<VoidGearItem> VOID_BOOTS = ITEMS.registerItem("void_boots",
            VoidGearItem::new, props -> props.humanoidArmor(TCMaterials.ARMOR_VOID, ArmorType.BOOTS));

    public static final DeferredItem<RobeArmorItem> CLOTH_CHEST = ITEMS.registerItem("cloth_chest",
            props -> new RobeArmorItem(3, props), props -> props.humanoidArmor(TCMaterials.ARMOR_ROBES, ArmorType.CHESTPLATE));
    public static final DeferredItem<RobeArmorItem> CLOTH_LEGS = ITEMS.registerItem("cloth_legs",
            props -> new RobeArmorItem(3, props), props -> props.humanoidArmor(TCMaterials.ARMOR_ROBES, ArmorType.LEGGINGS));
    public static final DeferredItem<RobeArmorItem> CLOTH_BOOTS = ITEMS.registerItem("cloth_boots",
            props -> new RobeArmorItem(2, props), props -> props.humanoidArmor(TCMaterials.ARMOR_ROBES, ArmorType.BOOTS));

    public static final DeferredItem<Item> BRAIN = ITEMS.registerItem(
            "brain",
            Item::new,
            props -> props
                    .food(new FoodProperties(4, 0.2F, true),
                            Consumables.defaultFood()
                                    .onConsume(new ApplyStatusEffectsConsumeEffect(
                                            new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8F))
                                    .build()));

    public static final DeferredItem<SpawnEggItem> BRAINY_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("brainy_zombie_spawn_egg", TCEntities.BRAINY_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> GIANT_BRAINY_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("giant_brainy_zombie_spawn_egg", TCEntities.GIANT_BRAINY_ZOMBIE);
    public static final DeferredItem<SpawnEggItem> FIREBAT_SPAWN_EGG =
            registerSpawnEgg("firebat_spawn_egg", TCEntities.FIRE_BAT);
    public static final DeferredItem<SpawnEggItem> MIND_SPIDER_SPAWN_EGG =
            registerSpawnEgg("mind_spider_spawn_egg", TCEntities.MIND_SPIDER);
    public static final DeferredItem<SpawnEggItem> WISP_SPAWN_EGG =
            registerSpawnEgg("wisp_spawn_egg", TCEntities.WISP);
    public static final DeferredItem<SpawnEggItem> THAUMIC_SLIME_SPAWN_EGG =
            registerSpawnEgg("thaumic_slime_spawn_egg", TCEntities.THAUMIC_SLIME);
    public static final DeferredItem<SpawnEggItem> TAINT_CRAWLER_SPAWN_EGG =
            registerSpawnEgg("taint_crawler_spawn_egg", TCEntities.TAINT_CRAWLER);
    public static final DeferredItem<SpawnEggItem> TAINTACLE_SPAWN_EGG =
            registerSpawnEgg("taintacle_spawn_egg", TCEntities.TAINTACLE);
    public static final DeferredItem<SpawnEggItem> TAINT_SWARM_SPAWN_EGG =
            registerSpawnEgg("taint_swarm_spawn_egg", TCEntities.TAINT_SWARM);
    public static final DeferredItem<SpawnEggItem> TAINT_SEED_SPAWN_EGG =
            registerSpawnEgg("taint_seed_spawn_egg", TCEntities.TAINT_SEED);
    public static final DeferredItem<SpawnEggItem> TAINT_SEED_PRIME_SPAWN_EGG =
            registerSpawnEgg("taint_seed_prime_spawn_egg", TCEntities.TAINT_SEED_PRIME);

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(String name, Supplier<? extends EntityType<?>> type) {
        return ITEMS.registerItem(name, properties -> new SpawnEggItem(properties.spawnEgg(type.get())));
    }

    //
    public static final DeferredItem<Item> TALLOW = ITEMS.registerSimpleItem("tallow");

    public static final Map<DyeColor, DeferredItem<BlockItem>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(dye, ITEMS.registerSimpleBlockItem(TCBlocks.CANDLES.get(dye)));
        }
    }

    public static final Map<DyeColor, DeferredItem<StandingAndWallBlockItem>> BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(dye, ITEMS.registerItem(
                    "banner_" + dye.getName(),
                    props -> new StandingAndWallBlockItem(
                            TCBlocks.BANNERS.get(dye).get(),
                            TCBlocks.WALL_BANNERS.get(dye).get(),
                            Direction.DOWN,
                            props.stacksTo(16).useBlockDescriptionPrefix())));
        }
    }

    public static final DeferredItem<StandingAndWallBlockItem> BANNER_CRIMSON_CULT = ITEMS.registerItem(
            "banner_crimson_cult",
            props -> new StandingAndWallBlockItem(
                    TCBlocks.BANNER_CRIMSON_CULT.get(),
                    TCBlocks.WALL_BANNER_CRIMSON_CULT.get(),
                    Direction.DOWN,
                    props.stacksTo(16).useBlockDescriptionPrefix()));

    public static final Map<DyeColor, DeferredItem<BlockItem>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, ITEMS.registerSimpleBlockItem(TCBlocks.NITORS.get(dye)));
        }
    }

    public static final DeferredItem<BlockItem> CRYSTAL_AER = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_AER);
    public static final DeferredItem<BlockItem> CRYSTAL_IGNIS = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_IGNIS);
    public static final DeferredItem<BlockItem> CRYSTAL_AQUA = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_AQUA);
    public static final DeferredItem<BlockItem> CRYSTAL_TERRA = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_TERRA);
    public static final DeferredItem<BlockItem> CRYSTAL_ORDO = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_ORDO);
    public static final DeferredItem<BlockItem> CRYSTAL_PERDITIO = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_PERDITIO);
    public static final DeferredItem<BlockItem> CRYSTAL_VITIUM = ITEMS.registerSimpleBlockItem(TCBlocks.CRYSTAL_VITIUM);

    // 

    public static final DeferredItem<BlockItem> STONE_ARCANE = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ARCANE);
    public static final DeferredItem<BlockItem> STONE_ARCANE_BRICK = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_TILE = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_TILE);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_ROCK = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_ROCK);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_GLYPHED = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_GLYPHED);
    public static final DeferredItem<BlockItem> STONE_ANCIENT_DOORWAY = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ANCIENT_DOORWAY);
    public static final DeferredItem<BlockItem> STONE_ELDRITCH_TILE = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_ELDRITCH_TILE);
    public static final DeferredItem<BlockItem> STONE_POROUS = ITEMS.registerSimpleBlockItem(TCBlocks.STONE_POROUS);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE = ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ARCANE);
    public static final DeferredItem<BlockItem> STAIRS_ARCANE_BRICK = ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ARCANE_BRICK);
    public static final DeferredItem<BlockItem> STAIRS_ANCIENT = ITEMS.registerSimpleBlockItem(TCBlocks.STAIRS_ANCIENT);

    // 

    public static final DeferredItem<BlockItem> SAPLING_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.SAPLING_GREATWOOD);
    public static final DeferredItem<BlockItem> SAPLING_SILVERWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.SAPLING_SILVERWOOD);
    public static final DeferredItem<BlockItem> LOG_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.LOG_GREATWOOD);
    public static final DeferredItem<BlockItem> LOG_SILVERWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.LOG_SILVERWOOD);
    public static final DeferredItem<BlockItem> LEAVES_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.LEAVES_GREATWOOD);
    public static final DeferredItem<BlockItem> LEAVES_SILVERWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.LEAVES_SILVERWOOD);
    public static final DeferredItem<BlockItem> PLANK_GREATWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.PLANK_GREATWOOD);
    public static final DeferredItem<BlockItem> PLANK_SILVERWOOD = ITEMS.registerSimpleBlockItem(TCBlocks.PLANK_SILVERWOOD);

    // 

    public static final DeferredItem<BlockItem> PLANT_SHIMMERLEAF = ITEMS.registerSimpleBlockItem(TCBlocks.PLANT_SHIMMERLEAF);
    public static final DeferredItem<BlockItem> PLANT_CINDERPEARL = ITEMS.registerSimpleBlockItem(TCBlocks.PLANT_CINDERPEARL);
    public static final DeferredItem<BlockItem> PLANT_VISHROOM = ITEMS.registerSimpleBlockItem(TCBlocks.PLANT_VISHROOM);
    public static final DeferredItem<BlockItem> GRASS_AMBIENT = ITEMS.registerSimpleBlockItem(TCBlocks.GRASS_AMBIENT);

    // RESOURCES | INGOTS

    public static final DeferredItem<BlockItem> ORE_AMBER = ITEMS.registerSimpleBlockItem(TCBlocks.ORE_AMBER);
    public static final DeferredItem<BlockItem> ORE_CINNABAR = ITEMS.registerSimpleBlockItem(TCBlocks.ORE_CINNABAR);
    public static final DeferredItem<BlockItem> ORE_QUARTZ = ITEMS.registerSimpleBlockItem(TCBlocks.ORE_QUARTZ);

    public static final DeferredItem<BlockItem> METAL_THAUMIUM_BLOCK = ITEMS.registerSimpleBlockItem(TCBlocks.METAL_THAUMIUM_BLOCK);
    public static final DeferredItem<BlockItem> METAL_BRASS_BLOCK = ITEMS.registerSimpleBlockItem(TCBlocks.METAL_BRASS_BLOCK);
    public static final DeferredItem<BlockItem> METAL_VOID_BLOCK = ITEMS.registerSimpleBlockItem(TCBlocks.METAL_VOID_BLOCK);
    public static final DeferredItem<BlockItem> AMBER_BLOCK = ITEMS.registerSimpleBlockItem(TCBlocks.AMBER_BLOCK);

    public static final DeferredItem<Item> INGOT_BRASS = ITEMS.registerSimpleItem("ingot_brass");
    public static final DeferredItem<Item> INGOT_THAUMIUM = ITEMS.registerSimpleItem("ingot_thaumium");
    public static final DeferredItem<Item> INGOT_VOID = ITEMS.registerSimpleItem("ingot_void");
    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber");

    public static final DeferredItem<Item> RARE_EARTH = ITEMS.registerSimpleItem("rare_earth");

    public static final DeferredItem<Item> NUGGET_BRASS = ITEMS.registerSimpleItem("nugget_brass");
    public static final DeferredItem<Item> NUGGET_THAUMIUM = ITEMS.registerSimpleItem("nugget_thaumium");
    public static final DeferredItem<Item> NUGGET_VOID = ITEMS.registerSimpleItem("nugget_void");
    public static final DeferredItem<Item> NUGGET_QUARTZ = ITEMS.registerSimpleItem("nugget_quartz");
    public static final DeferredItem<Item> NUGGET_QUICKSILVER = ITEMS.registerSimpleItem("nugget_quicksilver");

    public static final DeferredItem<Item> PLATE_IRON = ITEMS.registerSimpleItem("plate_iron");
    public static final DeferredItem<Item> PLATE_BRASS = ITEMS.registerSimpleItem("plate_brass");
    public static final DeferredItem<Item> PLATE_THAUMIUM = ITEMS.registerSimpleItem("plate_thaumium");
    public static final DeferredItem<Item> PLATE_VOID = ITEMS.registerSimpleItem("plate_void");

    public static final DeferredItem<Item> CLUSTER_IRON = ITEMS.registerSimpleItem("cluster_iron");
    public static final DeferredItem<Item> CLUSTER_GOLD = ITEMS.registerSimpleItem("cluster_gold");
    public static final DeferredItem<Item> CLUSTER_COPPER = ITEMS.registerSimpleItem("cluster_copper");
    public static final DeferredItem<Item> CLUSTER_TIN = ITEMS.registerSimpleItem("cluster_tin");
    public static final DeferredItem<Item> CLUSTER_SILVER = ITEMS.registerSimpleItem("cluster_silver");
    public static final DeferredItem<Item> CLUSTER_LEAD = ITEMS.registerSimpleItem("cluster_lead");
    public static final DeferredItem<Item> CLUSTER_CINNABAR = ITEMS.registerSimpleItem("cluster_cinnabar");
    public static final DeferredItem<Item> CLUSTER_QUARTZ = ITEMS.registerSimpleItem("cluster_quartz");

    // 

    public static final int SCRIBING_TOOLS_DURABILITY = 100;

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

    public static final DeferredItem<CelestialNotesItem> CELESTIAL_NOTES = ITEMS.registerItem(
            "celestial_notes",
            CelestialNotesItem::new,
            props -> props.component(TCDataComponents.CELESTIAL_BODY.get(), CelestialBody.SUN));

    // 

    public static final DeferredItem<PhialItem> PHIAL = ITEMS.registerItem(
            "phial",
            PhialItem::new
    );

    public static final DeferredItem<CausalityCollapserItem> CAUSALITY_COLLAPSER = ITEMS.registerItem(
            "causality_collapser",
            CausalityCollapserItem::new,
            props -> props.stacksTo(16));

    public static final DeferredItem<Item> VOID_SEED = ITEMS.registerItem(
            "void_seed",
            Item::new,
            props -> props.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<PrimordialPearlItem> PRIMORDIAL_PEARL = ITEMS.registerItem(
            "primordial_pearl",
            PrimordialPearlItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON).durability(PrimordialPearlItem.MAX_DAMAGE)
    );

    // 

    public static final int GOGGLES_DURABILITY = 350;

    public static final ResourceKey<EquipmentAsset> GOGGLES_REVEALING_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, TCIds.rl("goggles_revealing"));

    public static final DeferredItem<GogglesItem> GOGGLES_REVEALING = ITEMS.registerItem(
            "goggles_revealing",
            GogglesItem::new,
            props -> props
                    .stacksTo(1)
                    .durability(GOGGLES_DURABILITY)
                    .rarity(Rarity.RARE)
                    .component(
                            DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.HEAD)
                                    .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                                    .setAsset(GOGGLES_REVEALING_ASSET)
                                    .build()
                    ).attributes(
                            ItemAttributeModifiers.builder()
                                    .add(Attributes.ARMOR,
                                            new AttributeModifier(GOGGLES_REVEALING_ASSET.identifier(),1, AttributeModifier.Operation.ADD_VALUE),
                                            EquipmentSlotGroup.HEAD)
                                    .add(Attributes.ARMOR_TOUGHNESS,
                                            new AttributeModifier(GOGGLES_REVEALING_ASSET.identifier(),1, AttributeModifier.Operation.ADD_VALUE),
                                            EquipmentSlotGroup.HEAD)
                                    .build()
                    )
    );


    private TCItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static <T extends BlockItem> DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block, BiFunction<Block, Item.Properties, T> constructor) {
        return ITEMS.registerItem(block.unwrapKey().orElseThrow().identifier().getPath(), p->constructor.apply(block.value(),p), ()->new Item.Properties().useBlockDescriptionPrefix());
    }
}
