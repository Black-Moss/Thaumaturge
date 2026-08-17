package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only access to the world's effective aspect assignments for every {@link Item}.
 *
 * <p>
 * The index is rebuilt on every server resource reload by combining declared base aspects from the
 * {@code thaumaturge:base_aspects} datamap with aspects derived from registered recipes through
 * {@link IAspectRecipeContributor}s. The result is a flat {@link Item}-to-{@link AspectList} mapping suitable for
 * hot-path lookup.
 *
 * <p>
 * The index is synced to clients after reload and on player login, so client-side tooltip code can call it directly
 * without round-tripping to the server.
 *
 * <p>
 * Implementations are immutable. Querying an aspect for an item that has none returns {@link AspectList#EMPTY}.
 *
 * @since 1.0.0
 */
public interface IAspectIndex {
    /**
     * Looks up the aspect composition of the given item type.
     *
     * @param item the item to query
     * @return the aspect list, or {@link AspectList#EMPTY} when the item is unknown
     */
    AspectList of(Item item);

    /**
     * Looks up the aspect composition of the given stack. Per-stack data component aspects, if any, override the base index
     * entry for this stack.
     *
     * @param stack the stack to query
     * @return the effective aspect list, or {@link AspectList#EMPTY} when none
     */
    AspectList of(ItemStack stack);
}
