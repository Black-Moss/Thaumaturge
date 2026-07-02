package com.leclowndu93150.thaumcraft.data.recipe.builders;

import com.google.common.base.Preconditions;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.content.recipe.crucible.CrucibleRecipe;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrucibleRecipeBuilder extends SimpleRecipeBuilder {
    private final Ingredient catalyst;
    private @Nullable ResearchGate gate;
    private AspectList aspects = AspectList.EMPTY;

    public CrucibleRecipeBuilder(RecipeCategory category, ItemStackTemplate result, Ingredient catalyst) {
        super(result, category);
        this.catalyst = catalyst;
    }

    public CrucibleRecipeBuilder aspect(Holder<IAspect> aspect, int amount) {
        return aspect(new AspectInstance(aspect,amount));
    }

    public CrucibleRecipeBuilder aspect(AspectInstance instance) {
        Preconditions.checkArgument(instance.amount() > 0, "The amount of aspect must be positive !");
        this.aspects = aspects.add(instance);
        return this;
    }

    public CrucibleRecipeBuilder gate(ResearchGate gate) {
        Preconditions.checkNotNull(gate, "The research gate must not be null !");
        this.gate = gate;
        return this;
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        CrucibleRecipe recipe = new CrucibleRecipe(
                this.catalyst,
                this.aspects,
                this.result,
                Optional.ofNullable(gate));

        output.accept(key, recipe, this.advancementBuilder.build(output, key, this.category));
    }
}