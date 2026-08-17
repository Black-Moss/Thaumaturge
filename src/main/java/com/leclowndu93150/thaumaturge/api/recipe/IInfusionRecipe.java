package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An infusion altar recipe. The central pedestal holds the catalyst; the surrounding pedestals hold the components,
 * consumed one at a time while essentia drains from nearby containers.
 *
 * @since 1.0
 */
public interface IInfusionRecipe extends ResearchGated {
    /**
     * @return the ingredient required on the central pedestal
     */
    Ingredient catalyst();

    /**
     * @return the component ingredients, each consumed from a surrounding pedestal
     */
    List<Ingredient> components();

    /**
     * @return the essentia drained over the course of the craft, before cost modifiers
     */
    AspectList aspects();

    /**
     * @return the base instability of the craft; higher values destabilize the matrix faster
     */
    int instability();

    /**
     * @return a copy of the crafting result
     */
    ItemStack resultItem();

    /**
     * Matches the available pedestal stacks against {@link #components()}, one stack per component, order-insensitive.
     *
     * @param available the stacks present on the surrounding pedestals
     * @return single-count copies of the matched stacks in component order, or {@code null} when the stacks do not satisfy
     *         the components exactly
     */
    default List<ItemStack> matchComponents(List<ItemStack> available) {
        if (available.size() != components().size()) {
            return null;
        }
        List<ItemStack> remaining = new ArrayList<>(available);
        List<ItemStack> consumed = new ArrayList<>(components().size());
        for (Ingredient component : components()) {
            ItemStack found = null;
            for (ItemStack candidate : remaining) {
                if (component.test(candidate)) {
                    found = candidate;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            remaining.remove(found);
            consumed.add(found.copyWithCount(1));
        }
        return consumed;
    }
}
