package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectRecipeContributor;
import java.util.ArrayList;
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

public final class VanillaCraftingAspectContributor implements IAspectRecipeContributor {
    private static final CraftingInput EMPTY_INPUT = CraftingInput.of(0, 0, List.of());

    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        AspectList best = null;
        int bestSize = Integer.MAX_VALUE;

        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (!(recipe instanceof CraftingRecipe crafting)) {
                continue;
            }
            ItemStack output = safeAssemble(crafting);
            if (output.isEmpty() || output.getItem() != item) {
                continue;
            }
            AspectList candidate = aspectsFromIngredients(crafting.placementInfo().ingredients(), output, partial);
            if (candidate.isEmpty()) {
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

    private static ItemStack safeAssemble(CraftingRecipe recipe) {
        try {
            return recipe.assemble(EMPTY_INPUT);
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    private AspectList aspectsFromIngredients(List<Ingredient> ingredients, ItemStack output, IAspectIndex partial) {
        AspectList accumulator = AspectList.EMPTY;
        int outputCount = Math.max(1, output.getCount());

        for (Ingredient ingredient : ingredients) {
            ItemStack representative = representativeStack(ingredient);
            if (representative.isEmpty()) {
                continue;
            }
            AspectList aspects = partial.of(representative);
            for (AspectInstance entry : aspects.entries()) {
                accumulator = accumulator.add(entry);
            }
        }

        List<AspectInstance> scaled = new ArrayList<>(accumulator.size());
        for (AspectInstance entry : accumulator.entries()) {
            float scaledAmount = entry.amount() * 0.75f / outputCount;
            int rounded = scaledAmount >= 0.75f && scaledAmount < 1.0f ? 1 : (int) scaledAmount;
            if (rounded > 0) {
                scaled.add(new AspectInstance(entry.aspect(), rounded));
            }
        }
        return AspectList.ofEntries(scaled);
    }

    private static ItemStack representativeStack(Ingredient ingredient) {
        Optional<Holder<Item>> first = ingredient.items().findFirst();
        return first.map(itemHolder -> new ItemStack(itemHolder)).orElse(ItemStack.EMPTY);
    }
}
