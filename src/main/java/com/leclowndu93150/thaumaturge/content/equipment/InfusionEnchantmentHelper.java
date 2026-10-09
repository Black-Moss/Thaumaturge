package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public final class InfusionEnchantmentHelper {
    private InfusionEnchantmentHelper() {}

    private static DataComponentType<InfusionEnchantments> componentType() {
        return TTDataComponents.INFUSION_ENCHANTMENTS.get();
    }

    public static InfusionEnchantments get(ItemStack stack) {
        return Optional.ofNullable(stack.get(componentType())).orElse(InfusionEnchantments.EMPTY);
    }

    public static int level(ItemStack stack, InfusionEnchantment enchantment) {
        return get(stack).level(enchantment);
    }

    public static boolean has(ItemStack stack, InfusionEnchantment enchantment) {
        return get(stack).has(enchantment);
    }

    public static List<InfusionEnchantment> list(ItemStack stack) {
        return List.of(get(stack).levels().keySet().toArray(new InfusionEnchantment[0]));
    }

    public static void add(ItemStack stack, InfusionEnchantment enchantment, int level) {
        boolean acceptable = !stack.isEmpty() && level > 0 && level <= enchantment.maxLevel();
        if (!acceptable) {
            return;
        }
        InfusionEnchantments existing = get(stack);
        if (level > existing.level(enchantment)) {
            stack.set(componentType(), existing.with(enchantment, level));
        }
    }

    public static boolean canApply(ItemStack stack, InfusionEnchantment enchantment) {
        if (stack.isEmpty()) {
            return false;
        }
        return InfusionToolMatchers.matchesAny(enchantment.toolClasses(), stack);
    }
}
