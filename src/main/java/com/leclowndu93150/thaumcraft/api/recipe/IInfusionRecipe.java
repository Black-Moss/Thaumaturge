package com.leclowndu93150.thaumcraft.api.recipe;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An infusion altar recipe. The central pedestal holds the catalyst; the
 * surrounding pedestals hold the components, consumed one at a time while
 * essentia drains from nearby containers.
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
}
