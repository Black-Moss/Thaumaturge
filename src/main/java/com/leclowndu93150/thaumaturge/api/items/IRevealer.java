package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for items that reveal aura nodes when held or worn.
 *
 * <p>Implementers grant the wearer or holder visibility of aura nodes and similar invisible
 * entities.
 *
 * @since 1.0.0
 */
public interface IRevealer {
    /**
     * Returns whether the wearer or holder should see aura nodes through this item.
     *
     * @param stack  the held or worn stack
     * @param holder the entity holding or wearing the stack
     * @return {@code true} when nodes are revealed; {@code false} to suppress
     */
    boolean showNodes(ItemStack stack, LivingEntity holder);
}
