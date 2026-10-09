package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

public final class TTMaterials {
    private static final float NO_KNOCKBACK_RESISTANCE = 0.0F;
    private static final float TOUGHNESS_NONE = 0.0F;
    private static final float TOUGHNESS_LIGHT = 1.0F;
    private static final float TOUGHNESS_HEAVY = 2.0F;
    private static final float TOUGHNESS_FORTRESS = 3.0F;

    public static final ToolMaterial TOOL_THAUMIUM = tool(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22, TTItemTags.INGOTS_THAUMIUM);
    public static final ToolMaterial TOOL_VOID = tool(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0F, 3.0F, 10, TTItemTags.INGOTS_VOID_METAL);
    public static final ToolMaterial TOOL_ELEMENTAL = tool(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 9.0F, 3.0F, 18, TTItemTags.INGOTS_THAUMIUM);
    public static final ToolMaterial TOOL_PRIMAL_VOID = tool(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0F, 4.0F, 20, TTItemTags.INGOTS_VOID_METAL);
    public static final ToolMaterial TOOL_CRIMSON_VOID = tool(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 200, 8.0F, 3.5F, 20, TTItemTags.INGOTS_VOID_METAL);

    public static final ResourceKey<EquipmentAsset> ASSET_THAUMIUM = asset("thaumium");
    public static final ResourceKey<EquipmentAsset> ASSET_TRAVELLER = asset("traveller");
    public static final ResourceKey<EquipmentAsset> ASSET_ROBES = asset("robes");
    public static final ResourceKey<EquipmentAsset> ASSET_VOID = asset("void");
    public static final ResourceKey<EquipmentAsset> ASSET_VOID_ROBE = asset("void_robe");
    public static final ResourceKey<EquipmentAsset> ASSET_FORTRESS = asset("fortress");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_PLATE = asset("cultist_plate");
    public static final ResourceKey<EquipmentAsset> ASSET_ZOMBIE_PLATE = asset("zombie_plate");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_ROBE = asset("cultist_robe");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_BOOTS = asset("cultist_boots");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_LEADER = asset("cultist_leader");
    public static final ResourceKey<EquipmentAsset> ASSET_THAUMOSTATIC_HARNESS = asset("thaumostatic_harness");

    public static final ArmorMaterial ARMOR_THAUMIUM = armor(25, defense(2, 5, 6, 2), 25, SoundEvents.ARMOR_EQUIP_IRON, TOUGHNESS_LIGHT, TTItemTags.INGOTS_THAUMIUM, ASSET_THAUMIUM);
    public static final ArmorMaterial ARMOR_ROBES = armor(25, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, TOUGHNESS_LIGHT, ItemTags.WOOL, ASSET_ROBES);
    public static final ArmorMaterial ARMOR_TRAVELLER = armor(25, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, TOUGHNESS_LIGHT, Tags.Items.LEATHERS, ASSET_TRAVELLER);
    public static final ArmorMaterial ARMOR_VOID = armor(10, defense(3, 6, 8, 3), 10, SoundEvents.ARMOR_EQUIP_CHAIN, TOUGHNESS_LIGHT, TTItemTags.INGOTS_VOID_METAL, ASSET_VOID);
    public static final ArmorMaterial ARMOR_VOID_ROBE = armor(18, defense(4, 7, 9, 4), 10, SoundEvents.ARMOR_EQUIP_LEATHER, TOUGHNESS_HEAVY, TTItemTags.INGOTS_VOID_METAL, ASSET_VOID_ROBE);
    public static final ArmorMaterial ARMOR_FORTRESS = armor(40, defense(3, 6, 7, 3), 25, SoundEvents.ARMOR_EQUIP_IRON, TOUGHNESS_FORTRESS, TTItemTags.INGOTS_THAUMIUM, ASSET_FORTRESS);
    public static final ArmorMaterial ARMOR_CULTIST_PLATE = armor(18, defense(2, 5, 6, 2), 13, SoundEvents.ARMOR_EQUIP_IRON, TOUGHNESS_NONE, Tags.Items.INGOTS_IRON, ASSET_CULTIST_PLATE);
    public static final ArmorMaterial ARMOR_CULTIST_ROBE = armor(17, defense(2, 4, 5, 2), 13, SoundEvents.ARMOR_EQUIP_CHAIN, TOUGHNESS_NONE, ItemTags.WOOL, ASSET_CULTIST_ROBE);
    public static final ArmorMaterial ARMOR_CULTIST_BOOTS = armor(15, defense(2, 5, 6, 2), 9, SoundEvents.ARMOR_EQUIP_IRON, TOUGHNESS_NONE, Tags.Items.INGOTS_IRON, ASSET_CULTIST_BOOTS);
    public static final ArmorMaterial ARMOR_CULTIST_LEADER = armor(30, defense(3, 6, 7, 3), 20, SoundEvents.ARMOR_EQUIP_IRON, TOUGHNESS_LIGHT, Tags.Items.INGOTS_IRON, ASSET_CULTIST_LEADER);
    public static final ArmorMaterial ARMOR_THAUMOSTATIC_HARNESS = armor(25, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, TOUGHNESS_NONE, Tags.Items.INGOTS_GOLD,
            ASSET_THAUMOSTATIC_HARNESS);

    private TTMaterials() {}

    private static ToolMaterial tool(TagKey<Block> incorrectBlocks, int durability, float speed, float attackBonus, int enchantability, TagKey<Item> repair) {
        return new ToolMaterial(incorrectBlocks, durability, speed, attackBonus, enchantability, repair);
    }

    private static ResourceKey<EquipmentAsset> asset(String path) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, TTIds.rl(path));
    }

    private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        return Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, leggings, ArmorType.CHESTPLATE, chestplate, ArmorType.HELMET, helmet);
    }

    private static ArmorMaterial armor(int durability, Map<ArmorType, Integer> defense, int enchantability, Holder<SoundEvent> sound, float toughness, TagKey<Item> repair, ResourceKey<EquipmentAsset> asset) {
        return new ArmorMaterial(durability, defense, enchantability, sound, toughness, NO_KNOCKBACK_RESISTANCE, repair, asset);
    }
}
