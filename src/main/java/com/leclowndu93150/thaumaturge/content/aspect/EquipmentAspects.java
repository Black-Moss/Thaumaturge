package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

final class EquipmentAspects {
    private static final int ASPECT_PER_POINT = 4;
    private static final int SWORD_ATTACK_OFFSET = 2;
    private static final int SWORD_MIN_STEPS = 1;
    private static final int PROJECTILE_AVERSION = 10;
    private static final int PROJECTILE_FLIGHT = 5;
    private static final int[] DURABILITY_LIMITS = {59, 131, 250};
    private static final int[] TOOL_AMOUNTS = {4, 8, 12, 16};
    private static final List<TagKey<Item>> TOOL_TAGS = List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.HOES);

    private EquipmentAspects() {}

    static AspectList bonusFor(Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        List<Bonus> bonuses = new ArrayList<>();
        collectArmor(stack, bonuses);
        collectHeld(item, stack, bonuses);
        AspectList result = AspectList.EMPTY;
        for (Bonus bonus : bonuses) {
            result = withAmount(result, registries, bonus.key(), bonus.amount());
        }
        return result;
    }

    private static void collectArmor(ItemStack stack, List<Bonus> out) {
        if (isHumanoidArmor(stack)) {
            int armor = additiveSum(Attributes.ARMOR, stack);
            if (armor > 0) {
                out.add(new Bonus(TTAspects.PRAEMUNIO, armor * ASPECT_PER_POINT));
            }
        }
    }

    private static void collectHeld(Item item, ItemStack stack, List<Bonus> out) {
        if (stack.is(ItemTags.SWORDS)) {
            collectSword(stack, out);
        } else if (item instanceof ProjectileWeaponItem) {
            out.add(new Bonus(TTAspects.AVERSIO, PROJECTILE_AVERSION));
            out.add(new Bonus(TTAspects.VOLATUS, PROJECTILE_FLIGHT));
        } else if (isToolLike(stack)) {
            out.add(new Bonus(TTAspects.INSTRUMENTUM, toolAmount(stack.getMaxDamage())));
        }
    }

    private static void collectSword(ItemStack stack, List<Bonus> out) {
        int attack = additiveSum(Attributes.ATTACK_DAMAGE, stack);
        if (attack > 0) {
            int steps = Math.max(SWORD_MIN_STEPS, attack - SWORD_ATTACK_OFFSET);
            out.add(new Bonus(TTAspects.AVERSIO, steps * ASPECT_PER_POINT));
        }
    }

    private static boolean isHumanoidArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    private static boolean isToolLike(ItemStack stack) {
        return stack.is(Items.SHEARS) || TOOL_TAGS.stream().anyMatch(stack::is);
    }

    private static int toolAmount(int durability) {
        int tier = 0;
        while (tier < DURABILITY_LIMITS.length && durability > DURABILITY_LIMITS[tier]) {
            tier++;
        }
        return TOOL_AMOUNTS[tier];
    }

    private static int additiveSum(Holder<Attribute> attribute, ItemStack stack) {
        List<ItemAttributeModifiers.Entry> entries = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers();
        return (int) entries.stream().mapToDouble(entry -> additiveAmount(entry, attribute)).sum();
    }

    private static double additiveAmount(ItemAttributeModifiers.Entry entry, Holder<Attribute> attribute) {
        boolean additive = entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE;
        return additive && entry.attribute().is(attribute) ? entry.modifier().amount() : 0.0;
    }

    private static AspectList withAmount(AspectList list, HolderLookup.Provider registries, ResourceKey<IAspect> key, int amount) {
        Holder<IAspect> aspect = amount > 0 ? Aspects.resolve(registries, key) : null;
        return aspect == null ? list : list.add(aspect, amount);
    }

    private record Bonus(ResourceKey<IAspect> key, int amount) {
    }
}
