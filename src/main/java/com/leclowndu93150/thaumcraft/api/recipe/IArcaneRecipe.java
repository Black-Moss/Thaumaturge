package com.leclowndu93150.thaumcraft.api.recipe;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumcraft.registry.TCAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

/**
 *
 *
 * @since 1.0.0
 */

public interface IArcaneRecipe extends ResearchGated, Recipe<ArcaneCraftingInput> {
    int getBaseVis();

    AspectList getCrystals();
}
