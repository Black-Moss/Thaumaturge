package com.leclowndu93150.thaumcraft.api.essentia;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for items that store an {@link AspectList} as their essentia payload.
 *
 * <p>In modern Minecraft the payload travels through a {@link
 * net.minecraft.core.component.DataComponentType DataComponentType}, not item NBT; implementations
 * forward to the registered component.
 *
 * <p>Items exposing this interface should be registered against {@link EssentiaCapabilities#CONTAINER}.
 *
 * @since 1.0.0
 */
public interface IEssentiaContainerItem {
    /**
     * Reads the aspects stored on the given stack.
     *
     * @param stack the item stack, never {@code null}
     * @return the aspect list, never {@code null}; may be {@link AspectList#EMPTY}
     */
    AspectList getAspects(ItemStack stack);

    /**
     * Writes the given aspects onto the stack.
     *
     * @param stack   the item stack, never {@code null}
     * @param aspects the aspects to store
     */
    void setAspects(ItemStack stack, AspectList aspects);

    /**
     * Whether downstream systems should ignore the stored aspects when scanning the item.
     *
     * @return {@code true} when stored aspects should not contribute to item analysis
     */
    boolean ignoreContainedAspects();
}
