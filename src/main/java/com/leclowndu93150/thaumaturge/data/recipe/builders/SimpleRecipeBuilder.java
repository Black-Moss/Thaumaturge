package com.leclowndu93150.thaumaturge.data.recipe.builders;

import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;

import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.Nullable;

public abstract class SimpleRecipeBuilder implements RecipeBuilder {
    protected final ItemStackTemplate result;
    protected boolean showNotification = true;

    protected final RecipeUnlockAdvancementBuilder advancementBuilder;
    protected final RecipeCategory category;

    public SimpleRecipeBuilder(ItemStackTemplate result, RecipeCategory category) {
        this.result = result;
        this.category = category;
        this.advancementBuilder = new RecipeUnlockAdvancementBuilder();
    }

    @Override
    public SimpleRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name,criterion);
        return this;
    }

    @Override
    public SimpleRecipeBuilder group(@Nullable String group) {
        return this;
    }

    public SimpleRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }
}