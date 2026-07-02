package com.leclowndu93150.thaumcraft.content.recipe;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ThaumcraftCraftingManager {

    public static @Nullable CrucibleRecipe findMatchingCrucibleRecipe(ServerLevel level, Player player, AspectList aspects, ItemStack lastDrop) {
        int highest = 0;
        CrucibleRecipe out = null;

        List<CrucibleRecipe> recipes = level.recipeAccess().getRecipes().stream().filter(r -> r.value() instanceof CrucibleRecipe)
                .map(RecipeHolder::value)
                .map(CrucibleRecipe.class::cast)
                .filter(r -> r.matches(new CrucibleRecipeInput(lastDrop, aspects), level))
                .toList();

        for (CrucibleRecipe recipe : recipes) {
            if (player != null && recipe.doesPassGate(player)) {
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
