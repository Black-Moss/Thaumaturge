package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

final class RecipeAspectDerivation {
    private static final float INGREDIENT_SHARE = 0.75F;
    private static final float ROUNDED_UP_FROM = 0.75F;
    private static final float ROUNDED_UP_BELOW = 1.0F;
    private static final int MIN_OUTPUT_COUNT = 1;

    private RecipeAspectDerivation() {}

    static AspectList fromIngredients(List<Ingredient> ingredients, int outputCount, IAspectIndex partial) {
        AspectList totals = AspectList.EMPTY;
        for (Ingredient ingredient : ingredients) {
            ItemStack representative = representativeStack(ingredient);
            if (!representative.isEmpty()) {
                totals = totals.add(partial.of(representative));
            }
        }
        return scale(totals, outputCount);
    }

    static AspectList scale(AspectList totals, int outputCount) {
        int count = Math.max(MIN_OUTPUT_COUNT, outputCount);
        AspectList result = AspectList.EMPTY;
        for (AspectInstance entry : totals.entries()) {
            float value = entry.amount() * INGREDIENT_SHARE / count;
            int amount = value >= ROUNDED_UP_FROM && value < ROUNDED_UP_BELOW ? 1 : (int) value;
            if (amount > 0) {
                result = result.add(entry.aspect(), amount);
            }
        }
        return result;
    }

    static AspectList drain(AspectList cost, int outputCount) {
        int count = Math.max(MIN_OUTPUT_COUNT, outputCount);
        AspectList result = AspectList.EMPTY;
        for (AspectInstance entry : cost.entries()) {
            int amount = (int) (Math.sqrt(entry.amount()) / count);
            if (amount > 0) {
                result = result.add(entry.aspect(), amount);
            }
        }
        return result;
    }

    static ItemStack representativeStack(Ingredient ingredient) {
        return ingredient.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
