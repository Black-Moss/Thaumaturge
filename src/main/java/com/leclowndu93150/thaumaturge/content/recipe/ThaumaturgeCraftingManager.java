package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.recipes.TTClientRecipes;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ThaumaturgeCraftingManager {

    @SuppressWarnings("unchecked")
    public static @Nullable ArcaneCraftingRecipe findMatchingArcaneRecipe(Level level, ArcaneCraftingInput input, Player player) {
        RecipeMap recipes = level.isClientSide() ? TTClientRecipes.getRecipeMapForType(level, TTRecipeTypes.ARCANE.get()) : ((ServerLevel) level).recipeAccess().recipeMap();
        return recipes.byType(TTRecipeTypes.ARCANE.get()).stream().filter(r -> r.value().matches(input, level)).filter(r -> r.value().doesPassGate(player)).findFirst().map(RecipeHolder::value)
                .orElse(null);
    }

    public static @Nullable CrucibleRecipe findMatchingCrucibleRecipe(ServerLevel level, Player player, AspectList aspects, ItemStack lastDrop) {
        int highest = 0;
        CrucibleRecipe out = null;

        CrucibleRecipeInput input = new CrucibleRecipeInput(lastDrop, aspects);
        for (RecipeHolder<CrucibleRecipe> holder : level.recipeAccess().recipeMap().byType(TTRecipeTypes.CRUCIBLE.get())) {
            CrucibleRecipe recipe = holder.value();
            if (player != null && recipe.matches(input, level) && recipe.doesPassGate(player)) {
                int result = recipe.aspects().totalAmount();
                if (result > highest) {
                    highest = result;
                    out = recipe;
                }
            }
        }

        return out;
    }
}
