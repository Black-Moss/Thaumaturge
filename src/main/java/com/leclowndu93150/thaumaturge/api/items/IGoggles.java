package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for headgear that reveals invisible thaumic content.
 *
 * <p>
 * Worn on the head slot, an item implementing this interface enables in-world aura readouts, aspect overlays on items
 * and blocks, and node visibility. Helper code uses {@link com.leclowndu93150.thaumaturge.api.items.GogglesAccess} to
 * detect a wearer without depending on the concrete implementation.
 *
 * @since 1.0.0
 */
public interface IGoggles {
    /**
     * Returns whether the wearer should see in-game popups and HUD overlays for aspects and aura. The default Goggles of
     * Revealing implementation returns {@code true} unconditionally.
     *
     * @param stack the worn stack
     * @param wearer the entity wearing the stack
     * @return {@code true} when popups must show; {@code false} to suppress
     */
    boolean showIngamePopups(ItemStack stack, LivingEntity wearer);
}
