package com.leclowndu93150.thaumcraft.data.recipe;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumcraft.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

public final class TCRecipeProvider extends RecipeProvider {
    private TCRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    @Override
    protected void buildRecipes() {
        buildArcaneWorkbenchRecipes();
        buildCrucibleRecipes();
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

        shapeless(RecipeCategory.MISC,TCItems.NUGGET_QUARTZ,9)
                .requires(Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has",has(Tags.Items.GEMS_QUARTZ))
                .save(output);
    }

    private void buildCrucibleRecipes(){
        new CrucibleRecipeBuilder(RecipeCategory.MISC, new ItemStackTemplate(TCItems.NITORS.get(DyeColor.YELLOW).get()), Ingredient.of(Items.GLOWSTONE_DUST))
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"unlock_alchemy"), Optional.of(1),false))
                .aspect(getAspect(TCAspects.POTENTIA),10)
                .aspect(getAspect(TCAspects.IGNIS),10)
                .aspect(getAspect(TCAspects.LUX),10)
                .unlockedBy("has",has(Items.GLOWSTONE_DUST))
                .save(output);

        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect->{
            new CrucibleRecipeBuilder(RecipeCategory.MISC, new ItemStackTemplate(TCItems.ESSENTIA_CRYSTAL,1, DataComponentPatch.builder().set(TCDataComponents.CRYSTAL_ASPECT.get(),new AspectInstance(aspect,1)).build()), Ingredient.of(TCItems.NUGGET_QUARTZ))
                    .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"unlock_alchemy"), Optional.of(1),false))
                    .aspect(aspect,2)
                    .unlockedBy("has",has(TCItems.NUGGET_QUARTZ))
                    .save(output, TCIds.MODID + ":crucible/vis_crystal/"+ aspect.getKey().identifier().getPath());
        });
    }

    private void buildArcaneWorkbenchRecipes(){
        arcaneShaped(new ItemStackTemplate(TCItems.THAUMOMETER),20)
                .allAspects()
                .pattern(" G ")
                .pattern("GPG")
                .pattern(" G ")
                .define('G',Tags.Items.INGOTS_GOLD)
                .define('P',Tags.Items.GLASS_PANES)
                .unlockedBy("has",has(Tags.Items.INGOTS_GOLD))
                .save(output);
    }

    private Holder<IAspect> getAspect(ResourceKey<IAspect> key){
        return registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemStackTemplate result, int vis){
        return new ArcaneWorkbenchShapedRecipeBuilder(RecipeCategory.MISC,result,items, registries.lookupOrThrow(IAspect.REGISTRY_KEY),vis);
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemStackTemplate result, int vis){
        return new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.MISC,result,registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis,items);
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
