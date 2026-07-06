package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

final class RecipeAspectDerivation {
    private static final float INGREDIENT_SCALE = 0.75f;

    private RecipeAspectDerivation() {}

    static AspectList fromIngredients(List<Ingredient> ingredients, int outputCount, IAspectIndex partial) {
        AspectList accumulator = AspectList.EMPTY;
        for (Ingredient ingredient : ingredients) {
            ItemStack representative = representativeStack(ingredient);
            if (representative.isEmpty()) {
                continue;
            }
            for (AspectInstance entry : partial.of(representative).entries()) {
                accumulator = accumulator.add(entry);
            }
        }
        return scale(accumulator, outputCount);
    }

    static AspectList scale(AspectList accumulator, int outputCount) {
        int count = Math.max(1, outputCount);
        List<AspectInstance> scaled = new ArrayList<>(accumulator.size());
        for (AspectInstance entry : accumulator.entries()) {
            float value = entry.amount() * INGREDIENT_SCALE / count;
            int rounded = value >= INGREDIENT_SCALE && value < 1.0f ? 1 : (int) value;
            if (rounded > 0) {
                scaled.add(new AspectInstance(entry.aspect(), rounded));
            }
        }
        return AspectList.ofEntries(scaled);
    }

    static AspectList drain(AspectList recipeAspects, int outputCount) {
        int count = Math.max(1, outputCount);
        AspectList out = AspectList.EMPTY;
        for (AspectInstance entry : recipeAspects.entries()) {
            int amount = (int) (Math.sqrt(entry.amount()) / count);
            if (amount > 0) {
                out = out.add(entry.aspect(), amount);
            }
        }
        return out;
    }

    static ItemStack representativeStack(Ingredient ingredient) {
        Optional<Holder<Item>> first = ingredient.items().findFirst();
        return first.map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
