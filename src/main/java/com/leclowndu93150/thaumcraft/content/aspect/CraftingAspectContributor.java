package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingRecipe;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class CraftingAspectContributor implements IAspectRecipeContributor {
    private static final CraftingInput EMPTY_INPUT = CraftingInput.of(0, 0, List.of());

    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        Holder<IAspect> magic = registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.PRAECANTATIO);
        AspectList best = null;
        int bestSize = Integer.MAX_VALUE;

        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            Recipe<?> recipe = holder.value();
            AspectList candidate;
            if (recipe instanceof ArcaneCraftingRecipe arcane) {
                candidate = deriveArcane(arcane, item, partial, magic);
            } else if (recipe instanceof CraftingRecipe crafting) {
                candidate = deriveCrafting(crafting, item, partial);
            } else {
                continue;
            }
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            int size = candidate.totalAmount();
            if (size > 0 && size < bestSize) {
                best = candidate;
                bestSize = size;
            }
        }

        return Optional.ofNullable(best);
    }

    private static AspectList deriveCrafting(CraftingRecipe recipe, Item item, IAspectIndex partial) {
        ItemStack output = safeAssemble(recipe);
        if (output.isEmpty() || output.getItem() != item) {
            return null;
        }
        return RecipeAspectDerivation.fromIngredients(recipe.placementInfo().ingredients(), output.getCount(), partial);
    }

    private static AspectList deriveArcane(ArcaneCraftingRecipe recipe, Item item, IAspectIndex partial, Holder<IAspect> magic) {
        ItemStack output = safeAssembleArcane(recipe);
        if (output.isEmpty() || output.getItem() != item) {
            return null;
        }
        int count = Math.max(1, output.getCount());
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        AspectList out = RecipeAspectDerivation.fromIngredients(ingredients, count, partial);
        int vis = recipe.getBaseVis();
        if (vis > 0) {
            int bonus = (int) (Math.sqrt(1 + vis / 2) / count);
            if (bonus > 0) {
                out = out.add(magic, bonus);
            }
        }
        return out;
    }

    private static ItemStack safeAssemble(CraftingRecipe recipe) {
        try {
            ItemStack result = recipe.assemble(EMPTY_INPUT);
            return result == null ? ItemStack.EMPTY : result;
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack safeAssembleArcane(ArcaneCraftingRecipe recipe) {
        try {
            ItemStack result = recipe.assemble(ArcaneCraftingInput.EMPTY);
            return result == null ? ItemStack.EMPTY : result;
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }
}
