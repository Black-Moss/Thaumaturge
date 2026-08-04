package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

/**
 * An arcane workbench recipe, carrying its vis and crystal essentia cost alongside the standard crafting shape.
 *
 * @since 1.0.0
 */

public interface IArcaneRecipe extends ResearchGated, Recipe<IArcaneCraftingInput> {
    int getBaseVis();

    AspectList getCrystals();
}
