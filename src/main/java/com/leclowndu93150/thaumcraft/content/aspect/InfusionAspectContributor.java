package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumcraft.content.infusion.InfusionRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class InfusionAspectContributor implements IAspectRecipeContributor {
    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (!(recipe instanceof InfusionRecipe infusion)) {
                continue;
            }
            ItemStack output = infusion.rawResult().create();
            if (output.isEmpty() || output.getItem() != item) {
                continue;
            }
            List<Ingredient> ingredients = new ArrayList<>(infusion.components().size() + 1);
            ingredients.add(infusion.catalyst());
            ingredients.addAll(infusion.components());
            AspectList out = RecipeAspectDerivation.fromIngredients(ingredients, output.getCount(), partial);
            for (AspectInstance entry : RecipeAspectDerivation.drain(infusion.aspects(), output.getCount()).entries()) {
                out = out.add(entry);
            }
            if (!out.isEmpty()) {
                return Optional.of(out);
            }
        }
        return Optional.empty();
    }
}
