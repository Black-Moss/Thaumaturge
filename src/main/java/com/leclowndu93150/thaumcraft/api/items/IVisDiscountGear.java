package com.leclowndu93150.thaumcraft.api.items;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for worn gear that reduces vis cost for thaumaturgic actions.
 *
 * <p>The percentage returned by {@link #getVisDiscount(ItemStack)} is used to modify the player
 * attribute for the casting subsystem.
 *
 * @since 1.0.0
 */
public interface IVisDiscountGear {
    /**
     * Returns the percentage discount applied to vis-consuming casts when the player wears
     * this item. The Goggles of Revealing implementation returns {@code 5}.
     *
     * @param stack  the worn stack
     * @return the discount in whole percent units, never negative
     */
    int getVisDiscount(ItemStack stack);

    /**
     * Returns the group of equipment slots in which the Attribute Modifier should apply
     *
     * @param stack  the worn stack
     * @return the equipment slot group in which the attribute modifier should apply
     */
    default EquipmentSlotGroup getAppliedSlot(ItemStack stack) { return EquipmentSlotGroup.ANY; }
}
