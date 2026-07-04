package com.leclowndu93150.thaumcraft.data.recipe;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class TCRecipeProvider extends RecipeProvider {
    private TCRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    @Override
    protected void buildRecipes() {
        buildArcaneWorkbenchRecipes();
        buildBannerRecipes();
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

        shapeless(RecipeCategory.MISC, TCItems.NUGGET_QUARTZ, 9)
                .requires(Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        shaped(RecipeCategory.DECORATIONS, TCBlocks.CANDLES.get(DyeColor.WHITE).get(), 3)
                .pattern(" S ")
                .pattern(" T ")
                .pattern(" T ")
                .define('S', Tags.Items.STRINGS)
                .define('T', TCItems.TALLOW.get())
                .unlockedBy("has_tallow", has(TCItems.TALLOW.get()))
                .save(output);
        for (DyeColor dye : DyeColor.values()) {
            shapeless(RecipeCategory.DECORATIONS, TCBlocks.CANDLES.get(dye).get())
                    .requires(dyeTag(dye))
                    .requires(TCTags.CANDLES)
                    .unlockedBy("has_candle", has(TCTags.CANDLES))
                    .save(output, TCIds.MODID + ":candle_" + dye.getName() + "_from_dye");
        }
    }

    private void buildBannerRecipes() {
        for (DyeColor dye : DyeColor.values()) {
            arcaneShaped(new ItemStackTemplate(TCItems.BANNERS.get(dye).get()), 10)
                    .pattern("WS")
                    .pattern("WS")
                    .pattern("WB")
                    .define('W', wool(dye))
                    .define('S', Tags.Items.RODS_WOODEN)
                    .define('B', ItemTags.WOODEN_SLABS)
                    .unlockedBy("has_wool", has(ItemTags.WOOL))
                    .save(output, TCIds.MODID + ":arcane/banner_" + dye.getName());
        }
    }

    private static Item wool(DyeColor dye) {
        return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(dye.getName() + "_wool"));
    }

    private static TagKey<Item> dyeTag(DyeColor dye) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + dye.getName()));
    }

    private void buildCrucibleRecipes() {
        new CrucibleRecipeBuilder(RecipeCategory.MISC, new ItemStackTemplate(TCItems.TALLOW.get()), Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(getAspect(TCAspects.IGNIS), 1)
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        new CrucibleRecipeBuilder(RecipeCategory.MISC, new ItemStackTemplate(TCItems.NITORS.get(DyeColor.YELLOW).get()), Ingredient.of(Items.GLOWSTONE_DUST))
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_alchemy"), Optional.of(1), false))
                .aspect(getAspect(TCAspects.POTENTIA), 10)
                .aspect(getAspect(TCAspects.IGNIS), 10)
                .aspect(getAspect(TCAspects.LUX), 10)
                .unlockedBy("has", has(Items.GLOWSTONE_DUST))
                .save(output);

        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect -> {
            new CrucibleRecipeBuilder(RecipeCategory.MISC, new ItemStackTemplate(TCItems.ESSENTIA_CRYSTAL, 1, DataComponentPatch.builder().set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1)).build()), Ingredient.of(TCItems.NUGGET_QUARTZ))
                    .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_alchemy"), Optional.of(1), false))
                    .aspect(aspect, 2)
                    .unlockedBy("has", has(TCItems.NUGGET_QUARTZ))
                    .save(output, TCIds.MODID + ":crucible/vis_crystal/" + aspect.getKey().identifier().getPath());
        });
    }

    private void buildArcaneWorkbenchRecipes() {
        arcaneShaped(new ItemStackTemplate(TCItems.THAUMOMETER), 20)
                .allAspects()
                .pattern(" G ")
                .pattern("GPG")
                .pattern(" G ")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('P', Tags.Items.GLASS_PANES)
                .unlockedBy("has", has(Tags.Items.INGOTS_GOLD))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.VIS_RESONATOR), 50)
                .aspect(TCAspects.AER)
                .aspect(TCAspects.AQUA)
                .requires(Tags.Items.INGOTS_IRON)
                .requires(Tags.Items.GEMS_QUARTZ)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"unlock_auromancy"),Optional.of(1),false))
                .unlockedBy("has", has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.ARCANE_WORKBENCH_CHARGER), 200)
                .aspect(TCAspects.AER, 2)
                .aspect(TCAspects.ORDO, 2)
                .pattern(" R ")
                .pattern("P P")
                .pattern("I I")
                .define('R', TCItems.VIS_RESONATOR)
                .define('P', TCItems.PLANK_GREATWOOD)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.GOGGLES_REVEALING),50)
                .pattern("LBL")
                .pattern("L L")
                .pattern("MBM")
                .define('L',Tags.Items.LEATHERS)
                .define('B',TCItems.INGOT_BRASS)
                .define('M',TCItems.THAUMOMETER)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"unlock_artifice"),Optional.of(1),false))
                .unlockedBy("has",has(TCItems.THAUMOMETER))
                .save(output);
    }

    private Holder<IAspect> getAspect(ResourceKey<IAspect> key) {
        return registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapedRecipeBuilder(RecipeCategory.MISC, result, items, registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis);
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.MISC, result, registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis, items);
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
