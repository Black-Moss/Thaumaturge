package com.leclowndu93150.thaumcraft.api.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for worn gear that reduces vis cost for thaumaturgic actions.
 *
 * <p>The percentage returned by {@link #getVisDiscount(ItemStack, Player)} is summed across all
 * worn pieces by the casting subsystem.
 *
 * @since 1.0.0
 */
public interface IVisDiscountGear {
    /**
     * Returns the percentage discount applied to vis-consuming casts when the player wears
     * this item. The Goggles of Revealing implementation returns {@code 5}.
     *
     * @param stack  the worn stack
     * @param player the wearing player
     * @return the discount in whole percent units, never negative
     */
    int getVisDiscount(ItemStack stack, Player player);
}
