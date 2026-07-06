package com.leclowndu93150.thaumcraft.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Marks an item that stores a vis charge and can be recharged from the aura.
 *
 * <p>Charge is persisted on the stack through the {@code thaumcraft:charge} data component and
 * manipulated through {@link RechargeAccess}. Implementations declare their capacity and whether
 * a charge indicator is shown while the item is held.
 *
 * @since 1.0.0
 */
public interface IRechargable {
    /**
     * Returns the maximum charge this stack can hold.
     *
     * @param stack the stack under query
     * @param holder the entity holding the stack, or {@code null} when queried out of world
     * @return the capacity in vis units, never negative
     */
    int getMaxCharge(ItemStack stack, LivingEntity holder);

    /**
     * Returns how a charge indicator should be shown for this stack while held.
     *
     * @param stack the stack under query
     * @param holder the entity holding the stack, or {@code null} when queried out of world
     * @return the display mode, never {@code null}
     */
    ChargeDisplay showInHud(ItemStack stack, LivingEntity holder);

    /**
     * Charge indicator display modes.
     *
     * @since 1.0.0
     */
    enum ChargeDisplay {
        /** No indicator is ever shown. */
        NEVER,
        /** The indicator is shown whenever the stack is held. */
        NORMAL,
        /** The indicator is shown briefly after the charge changes. */
        PERIODIC
    }
}
