package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.recipes.TTClientRecipes;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ThaumaturgeCraftingManager {
    public ThaumaturgeCraftingManager() {}

    public static @Nullable ArcaneCraftingRecipe findMatchingArcaneRecipe(Level level, ArcaneCraftingInput input, Player player) {
        return arcaneRecipes(level).byType(TTRecipeTypes.ARCANE.get()).stream().map(RecipeHolder::value).filter(candidate -> candidate.matches(input, level) && candidate.doesPassGate(player))
                .findFirst().orElse(null);
    }

    public static @Nullable CrucibleRecipe findMatchingCrucibleRecipe(ServerLevel level, @Nullable Player player, AspectList aspects, ItemStack catalyst) {
        return Optional.ofNullable(player).map(gated -> richestCrucibleMatch(level, gated, new CrucibleRecipeInput(catalyst, aspects))).orElse(null);
    }

    private static @Nullable CrucibleRecipe richestCrucibleMatch(ServerLevel level, Player player, CrucibleRecipeInput input) {
        return level.recipeAccess().recipeMap().byType(TTRecipeTypes.CRUCIBLE.get()).stream().map(RecipeHolder::value)
                .filter(candidate -> candidate.matches(input, level) && candidate.doesPassGate(player)).filter(candidate -> candidate.aspects().totalAmount() > 0)
                .max(Comparator.comparingInt(candidate -> candidate.aspects().totalAmount())).orElse(null);
    }

    private static RecipeMap arcaneRecipes(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().recipeMap();
        }
        return TTClientRecipes.getRecipeMapForType(level, TTRecipeTypes.ARCANE.get());
    }
}
