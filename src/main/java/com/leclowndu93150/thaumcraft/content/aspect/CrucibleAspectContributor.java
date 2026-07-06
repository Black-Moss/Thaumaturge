package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumcraft.content.recipe.crucible.CrucibleRecipe;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class CrucibleAspectContributor implements IAspectRecipeContributor {
    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (!(recipe instanceof CrucibleRecipe crucible)) {
                continue;
            }
            ItemStack output = crucible.rawResult().create();
            if (output.isEmpty() || output.getItem() != item) {
                continue;
            }
            ItemStack catalyst = RecipeAspectDerivation.representativeStack(crucible.catalyst());
            if (catalyst.isEmpty()) {
                continue;
            }
            AspectList out = partial.of(catalyst);
            for (AspectInstance entry : RecipeAspectDerivation.drain(crucible.aspects(), output.getCount()).entries()) {
                out = out.add(entry);
            }
            if (!out.isEmpty()) {
                return Optional.of(out);
            }
        }
        return Optional.empty();
    }
}
