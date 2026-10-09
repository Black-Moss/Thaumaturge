package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

public final class InfusionToolMatchers {
    private static final String TOKEN_ALL = "all";
    private static final String TOKEN_WEAPON = "weapon";
    private static final String TOKEN_PICKAXE = "pickaxe";
    private static final String TOKEN_AXE = "axe";
    private static final String TOKEN_SHOVEL = "shovel";
    private static final String TOKEN_HOE = "hoe";
    private static final String TOKEN_ARMOR = "armor";
    private static final String TOKEN_RECHARGEABLE = "rechargeable";
    private static final String TOKEN_CHARGABLE = "chargable";
    private static final Map<EquipmentSlot, String> SLOT_TOKEN_OVERRIDES = Map.of(EquipmentSlot.HEAD, "helm", EquipmentSlot.FEET, "boots");
    private static final Map<String, Predicate<ItemStack>> MATCHERS = buildMatchers();

    private InfusionToolMatchers() {}

    public static boolean matchesAny(Set<String> tokens, ItemStack stack) {
        for (String token : tokens) {
            Predicate<ItemStack> matcher = MATCHERS.get(token);
            if (matcher != null && matcher.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Predicate<ItemStack>> buildMatchers() {
        Map<String, Predicate<ItemStack>> matchers = new HashMap<>();
        matchers.put(TOKEN_ALL, stack -> true);
        matchers.put(TOKEN_WEAPON, InfusionToolMatchers::dealsMainHandDamage);
        matchers.put(TOKEN_PICKAXE, stack -> stack.is(ItemTags.PICKAXES));
        matchers.put(TOKEN_AXE, stack -> stack.is(ItemTags.AXES));
        matchers.put(TOKEN_SHOVEL, stack -> stack.is(ItemTags.SHOVELS));
        matchers.put(TOKEN_HOE, stack -> stack.is(ItemTags.HOES));
        matchers.put(TOKEN_ARMOR, stack -> stack.has(DataComponents.EQUIPPABLE));
        matchers.put(TOKEN_RECHARGEABLE, RechargeAccess::isRechargeable);
        matchers.put(TOKEN_CHARGABLE, RechargeAccess::isRechargeable);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                matchers.put(SLOT_TOKEN_OVERRIDES.getOrDefault(slot, slot.getName()), stack -> equippedIn(stack, slot));
            }
        }
        return Map.copyOf(matchers);
    }

    private static boolean equippedIn(ItemStack stack, EquipmentSlot slot) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == slot;
    }

    private static boolean dealsMainHandDamage(ItemStack stack) {
        for (ItemAttributeModifiers.Entry entry : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot().test(EquipmentSlot.MAINHAND)) {
                return true;
            }
        }
        return false;
    }
}
