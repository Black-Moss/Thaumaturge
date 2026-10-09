package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ArcaneBoreTool {
    private static final int ENCHANTABILITY_DIVISOR = 3;
    private static final int RADIUS_PER_DESTRUCTIVE = 2;
    private static final int MIN_RADIUS = 2;
    private static final int DEPTH_PER_RADIUS = 8;
    private static final int DEPTH_PER_BURROWING = 16;
    private static final float SPEED_DIVISOR = 2.0F;
    private static final int KEPT_DURABILITY = 1;

    private ArcaneBoreTool() {}

    public static boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        }
        for (Tool.Rule rule : tool.rules()) {
            if (rule.blocks().unwrapKey().filter(BlockTags.MINEABLE_WITH_PICKAXE::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public static boolean valid(ItemStack stack) {
        return isPickaxe(stack) && (!stack.isDamageableItem() || stack.getDamageValue() + KEPT_DURABILITY < stack.getMaxDamage());
    }

    public static int digRadius(ItemStack stack) {
        int radius = 0;
        if (isPickaxe(stack)) {
            Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
            int base = enchantable == null ? 0 : enchantable.value() / ENCHANTABILITY_DIVISOR;
            radius = base + RADIUS_PER_DESTRUCTIVE * InfusionEnchantmentHelper.level(stack, InfusionEnchantment.DESTRUCTIVE);
        }
        return radius <= 1 ? MIN_RADIUS : radius;
    }

    public static int digDepth(ItemStack stack) {
        return DEPTH_PER_RADIUS * digRadius(stack) + DEPTH_PER_BURROWING * InfusionEnchantmentHelper.level(stack, InfusionEnchantment.BURROWING);
    }

    public static int fortune(Level level, ItemStack stack) {
        if (!valid(stack)) {
            return 0;
        }
        return Math.max(enchantLevel(level, stack, Enchantments.FORTUNE), InfusionEnchantmentHelper.level(stack, InfusionEnchantment.SOUNDING));
    }

    public static int digSpeed(Level level, ItemStack stack, BlockState state) {
        if (!valid(stack)) {
            return 0;
        }
        return (int) (stack.getDestroySpeed(state) / SPEED_DIVISOR) + enchantLevel(level, stack, Enchantments.EFFICIENCY);
    }

    public static int refining(ItemStack stack) {
        return InfusionEnchantmentHelper.level(stack, InfusionEnchantment.REFINING);
    }

    public static boolean silkTouch(Level level, ItemStack stack) {
        return !stack.isEmpty() && enchantLevel(level, stack, Enchantments.SILK_TOUCH) > 0;
    }

    private static int enchantLevel(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }
}
