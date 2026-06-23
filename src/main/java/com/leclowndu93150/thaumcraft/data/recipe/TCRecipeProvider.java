package com.leclowndu93150.thaumcraft.data.recipe;

import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

public final class TCRecipeProvider extends RecipeProvider {
    private TCRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.MISC, TCItems.JAR_BRACE.get(), 2)
                .define('N', Items.IRON_NUGGET)
                .pattern("N N")
                .pattern("N N")
                .pattern("NNN")
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(output);

        shaped(RecipeCategory.REDSTONE, TCItems.TUBE.get(), 8)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .pattern("IGI")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.REDSTONE, TCItems.TUBE_VALVE.get(), 1)
                .define('T', TCItems.TUBE.get())
                .define('R', Items.REDSTONE)
                .pattern("R")
                .pattern("T")
                .unlockedBy("has_tube", has(TCItems.TUBE.get()))
                .save(output);

        shaped(RecipeCategory.REDSTONE, TCItems.TUBE_RESTRICT.get(), 1)
                .define('T', TCItems.TUBE.get())
                .define('N', Items.IRON_NUGGET)
                .pattern("N")
                .pattern("T")
                .pattern("N")
                .unlockedBy("has_tube", has(TCItems.TUBE.get()))
                .save(output);

        shaped(RecipeCategory.REDSTONE, TCItems.TUBE_FILTER.get(), 1)
                .define('T', TCItems.TUBE.get())
                .define('H', Items.HOPPER)
                .pattern("H")
                .pattern("T")
                .unlockedBy("has_tube", has(TCItems.TUBE.get()))
                .save(output);

        shaped(RecipeCategory.REDSTONE, TCItems.TUBE_ONEWAY.get(), 1)
                .define('T', TCItems.TUBE.get())
                .define('P', Items.PISTON)
                .pattern("P")
                .pattern("T")
                .unlockedBy("has_tube", has(TCItems.TUBE.get()))
                .save(output);

        shaped(RecipeCategory.REDSTONE, TCItems.TUBE_BUFFER.get(), 1)
                .define('T', TCItems.TUBE.get())
                .define('I', Items.IRON_INGOT)
                .pattern("ITI")
                .pattern("ITI")
                .unlockedBy("has_tube", has(TCItems.TUBE.get()))
                .save(output);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new TCRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Thaumcraft Recipes";
        }
    }
}
