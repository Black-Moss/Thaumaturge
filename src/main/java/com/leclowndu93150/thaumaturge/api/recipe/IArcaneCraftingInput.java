package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jspecify.annotations.Nullable;

/**
 * The crafting grid presented to an {@link IArcaneRecipe}, combining a vanilla
 * {@link RecipeInput} grid with the workbench state a recipe needs to price itself.
 *
 * <p>Implementations are supplied by the arcane workbench; addons match against this type
 * rather than the concrete container.
 *
 * @since 1.0.0
 */
public interface IArcaneCraftingInput extends RecipeInput, IArcaneWorkbench {
    /**
     * The stack at the given grid position.
     *
     * @param x the column, zero-based
     * @param y the row, zero-based
     * @return the stack, or {@link ItemStack#EMPTY} when the slot is empty
     */
    ItemStack getItem(int x, int y);

    /**
     * The width of the occupied grid region.
     *
     * @return the width in slots
     */
    int width();

    /**
     * The height of the occupied grid region.
     *
     * @return the height in slots
     */
    int height();

    /**
     * The player performing the craft, when one is known. Recipes use this to resolve
     * research gating and vis discounts.
     *
     * @return the crafting player, or null when the craft is not player-driven
     */
    @Nullable
    Player player();

    /**
     * The number of non-empty stacks in the crafting grid.
     *
     * @return the ingredient count
     */
    int ingredientCount();

    /**
     * The backing stacks, grid slots first followed by the crystal and wand slots.
     *
     * @return the stacks, in slot order
     */
    List<ItemStack> items();

    /**
     * The grid contents indexed for recipe matching.
     *
     * @return the stacked contents
     */
    StackedItemContents stackedContents();
}
