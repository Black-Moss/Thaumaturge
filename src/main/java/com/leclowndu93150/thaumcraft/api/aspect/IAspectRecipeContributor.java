package com.leclowndu93150.thaumcraft.api.aspect;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Strategy that derives an {@link AspectList} for an item from registered recipes.
 *
 * <p>Contributors run during server-side index build. They are invoked once per item that has
 * no explicit base aspects in the {@code thaumcraft:base_aspects} datamap. They may consult
 * the current {@link RecipeManager} and the partial {@link IAspectIndex} that already contains
 * resolved entries from datamap, vanilla items, and contributors that ran earlier.
 *
 * <p>Contributors must be deterministic with respect to their inputs. They must not depend on
 * world state, time, or random sources. Returning {@link Optional#empty()} signals that this
 * contributor has nothing to say about the given item, allowing later contributors to try.
 *
 * <p>Built-in contributors cover the crucible, infusion, and crafting recipe types, in that
 * priority order. The crafting walker also handles arcane workbench recipes, adding a magic
 * component derived from their vis cost.
 *
 * @since 1.0.0
 */
public interface IAspectRecipeContributor {
    /**
     * Attempts to derive aspects for an item from recipes.
     *
     * @param item the item under analysis
     * @param recipes the active recipe manager
     * @param registries the holder lookup provider for the current reload
     * @param partial the partial index, already populated with base aspects and prior results
     * @return the derived aspects, or {@link Optional#empty()} when this contributor abstains
     */
    Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial);
}
