package com.leclowndu93150.thaumcraft.data.recipe;

import com.google.common.collect.ImmutableList;
import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.InfusionRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumcraft.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.mixin.data.recipes.RecipeProviderAccessor;
import com.leclowndu93150.thaumcraft.registry.TCBlockFamilies;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItemTags;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.CustomCraftingRecipeBuilder;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

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
        buildGearRecipes();
        buildInfusionAltarRecipes();
        buildFocalManipulatorRecipe();
        buildCrucibleRecipes();
        buildFocusRecipes();

        shapeless(RecipeCategory.MISC,TCItems.SCRIBING_TOOLS)
                .requires(TCItems.PHIAL)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.FEATHERS)
                .unlockedBy("has", has(TCItems.PHIAL))
                .save(output);

        shapeless(RecipeCategory.MISC,TCItems.SCRIBING_TOOLS)
                .requires(Items.GLASS_BOTTLE)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.FEATHERS)
                .unlockedBy("has", has(Tags.Items.GLASS_PANES))
                .save(output, TCIds.MODID+":scribing_tools_alt");

        shapeless(RecipeCategory.MISC,TCItems.LABEL)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.PAPER,4)
                .unlockedBy("has", has(Tags.Items.SLIME_BALLS))
                .save(output);

        shapeless(RecipeCategory.MISC,TCItems.LABEL)
                .requires(TCItems.LABEL)
                .unlockedBy("has", has(TCItems.LABEL))
                .save(output,TCIds.MODID+":label_clear");

        shaped(RecipeCategory.MISC,TCItems.JAR_BRACE,2)
                .pattern("SBS")
                .pattern("B B")
                .pattern("SBS")
                .define('S',Tags.Items.RODS_WOODEN)
                .define('B',TCItemTags.NUGGETS_BRASS)
                .unlockedBy("has", has(TCItems.NUGGET_BRASS))
                .save(output);

        for (DyeColor color : DyeColor.values()){
            shapeless(RecipeCategory.MISC,TCItems.NITORS.get(color).get())
                    .requires(TCItemTags.NITORS)
                    .requires(color.getTag())
                    .unlockedBy("has", has(TCItemTags.NITORS))
                    .save(output, TCIds.MODID+":nitors/"+color.getName());
        }

        shaped(RecipeCategory.BUILDING_BLOCKS,TCItems.STONE_ARCANE,8)
                .pattern("SSS")
                .pattern("SVS")
                .pattern("SSS")
                .define('S', Tags.Items.STONES)
                .define('V',TCItems.ESSENTIA_CRYSTAL)
                .unlockedBy("has", has(TCItems.ESSENTIA_CRYSTAL))
                .save(output);

        TCBlockFamilies.getAllFamilies().forEach(family -> {
            family.getVariants().forEach((variant, result) -> {
                    if (family.shouldGenerateCraftingRecipe()) {
                        ItemLike base = this.getBaseBlockForCrafting(family, variant);
                        ((RecipeProviderAccessor)(RecipeProvider)this).thaumcraft$generateCraftingRecipe(family, variant, result, base);
                        if (variant == BlockFamily.Variant.CRACKED) {
                            this.smeltingResultFromBase(result, base);
                        }
                    }

                    if (family.shouldGenerateStonecutterRecipe()) {
                        Block base = family.getBaseBlock();
                        ((RecipeProviderAccessor)(RecipeProvider)this).thaumcraft$generateStonecutterRecipe(family, variant, base);
                    }

            });
        });

        shaped(RecipeCategory.MISC,TCItems.PHIAL,8)
                .pattern(" C ")
                .pattern("P P")
                .pattern(" P ")
                .define('C', Items.CLAY_BALL)
                .define('P', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has", has(Tags.Items.GLASS_BLOCKS))
                .save(output);


        oreSmelting(TCItems.QUICKSILVER,TCItemTags.ORES_CINNABAR,1F,"quicksilver");
        oreSmelting(TCItems.AMBER,TCItemTags.ORES_AMBER,1F,"amber");
        oreSmelting(Items.QUARTZ,Tags.Items.ORES_QUARTZ,0.2F,"quartz");


        block3x3(TCItems.METAL_BRASS_BLOCK,TCItemTags.INGOTS_BRASS,TCItems.INGOT_BRASS,TCItemTags.STORAGE_BLOCKS_BRASS);
        block3x3(TCItems.METAL_THAUMIUM_BLOCK,TCItemTags.INGOTS_THAUMIUM,TCItems.INGOT_THAUMIUM,TCItemTags.STORAGE_BLOCKS_THAUMIUM);
        block3x3(TCItems.METAL_VOID_BLOCK,TCItemTags.INGOTS_VOID_METAL,TCItems.INGOT_VOID,TCItemTags.STORAGE_BLOCKS_VOID_METAL);
        block2x2(TCItems.AMBER_BLOCK,TCItemTags.GEMS_AMBER,TCItems.AMBER,TCItemTags.STORAGE_BLOCKS_AMBER);

        nuggets3x3(Items.QUARTZ,TCItemTags.NUGGETS_QUARTZ,TCItems.NUGGET_QUARTZ,Tags.Items.GEMS_QUARTZ);
        nuggets3x3(TCItems.QUICKSILVER,TCItemTags.NUGGETS_QUICKSILVER,TCItems.NUGGET_QUICKSILVER,TCItemTags.GEMS_QUICKSILVER);
        nuggets3x3(TCItems.INGOT_BRASS,TCItemTags.NUGGETS_BRASS,TCItems.NUGGET_BRASS,TCItemTags.INGOTS_BRASS);
        nuggets3x3(TCItems.INGOT_THAUMIUM,TCItemTags.NUGGETS_THAUMIUM,TCItems.NUGGET_THAUMIUM,TCItemTags.INGOTS_THAUMIUM);
        nuggets3x3(TCItems.INGOT_VOID,TCItemTags.NUGGETS_VOID_METAL,TCItems.NUGGET_VOID,TCItemTags.INGOTS_VOID_METAL);

        plateRecipe(TCItems.PLATE_IRON,Tags.Items.INGOTS_IRON);
        plateRecipe(TCItems.PLATE_BRASS,TCItemTags.INGOTS_BRASS);
        plateRecipe(TCItems.PLATE_THAUMIUM,TCItemTags.INGOTS_THAUMIUM);
        plateRecipe(TCItems.PLATE_VOID,TCItemTags.INGOTS_VOID_METAL);

        clusterSmelting(Items.IRON_INGOT,TCItems.CLUSTER_IRON,"iron_ingot");
        clusterSmelting(Items.GOLD_INGOT,TCItems.CLUSTER_GOLD,"gold_ingot");
        clusterSmelting(Items.COPPER_INGOT,TCItems.CLUSTER_COPPER,"copper_ingot");
        clusterSmelting(TCItems.QUICKSILVER,TCItems.CLUSTER_CINNABAR,"quicksilver");
        clusterSmelting(Items.QUARTZ,TCItems.CLUSTER_QUARTZ,"quartz");

        shapeless(RecipeCategory.MISC,TCItems.QUICKSILVER)
                .requires(TCItems.PLANT_SHIMMERLEAF)
                .unlockedBy("has",has(TCItems.PLANT_SHIMMERLEAF))
                .save(output,TCIds.MODID+":quicksilver_from_shimmerleaf");

        shapeless(RecipeCategory.MISC,Items.BLAZE_POWDER)
                .requires(TCItems.PLANT_CINDERPEARL)
                .unlockedBy("has",has(TCItems.PLANT_CINDERPEARL))
                .save(output,TCIds.MODID+":blaze_powder_from_cinderpearl");

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
                    .requires(TCItemTags.CANDLES)
                    .unlockedBy("has_candle", has(TCItemTags.CANDLES))
                    .save(output, TCIds.MODID + ":candle_" + dye.getName() + "_from_dye");
        }

    }

    private void block3x3(ItemLike block, TagKey<Item> baseTag, ItemLike baseItem, TagKey<Item> blockTag){
        shaped(RecipeCategory.MISC,block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#',baseTag)
                .unlockedBy("has", has(baseTag))
                .save(output);

        shapeless(RecipeCategory.MISC,baseItem,9)
                .requires(blockTag)
                .unlockedBy("has", has(blockTag))
                .save(output, TCIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(baseItem.asItem()).getPath() + "_from_block");
    }

    private void block2x2(ItemLike block, TagKey<Item> baseTag, ItemLike baseItem, TagKey<Item> blockTag){
        shaped(RecipeCategory.MISC,block)
                .pattern("##")
                .pattern("##")
                .define('#',baseTag)
                .unlockedBy("has", has(baseTag))
                .save(output);

        shapeless(RecipeCategory.MISC,baseItem,4)
                .requires(blockTag)
                .unlockedBy("has", has(blockTag))
                .save(output, TCIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(baseItem.asItem()).getPath() + "_from_block");
    }

    private void nuggets3x3(ItemLike item, TagKey<Item> nuggetsTag, ItemLike nuggets, TagKey<Item> itemTag){
        shaped(RecipeCategory.MISC,item)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#',nuggetsTag)
                .unlockedBy("has", has(nuggetsTag))
                .save(output,TCIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(nuggets.asItem()).getPath() + "_from_nuggets");

        shapeless(RecipeCategory.MISC,nuggets,9)
                .requires(itemTag)
                .unlockedBy("has", has(itemTag))
                .save(output);
    }

    private void oreSmelting(ItemLike item, TagKey<Item> oreTag, float xp,String group){
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(items.getOrThrow(oreTag)), RecipeCategory.MISC, CookingBookCategory.MISC, item,xp, 200)
                .group(group)
                .unlockedBy("has", this.has(oreTag))
                .save(this.output, getItemName(item) + "_from_ore");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(items.getOrThrow(oreTag)), RecipeCategory.MISC, CookingBookCategory.MISC, item,xp, 100)
                .group(group)
                .unlockedBy("has", this.has(oreTag))
                .save(this.output, getItemName(item) + "_blasting_from_ore");
    }

    private void clusterSmelting(ItemLike item, ItemLike cluster,String group){

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(cluster), RecipeCategory.MISC, CookingBookCategory.MISC, new ItemStackTemplate(item.asItem(),2),1F, 200)
                .group(group)
                .unlockedBy("has", this.has(cluster))
                .save(this.output, getItemName(item) + "_from_cluster");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(cluster), RecipeCategory.MISC, CookingBookCategory.MISC, new ItemStackTemplate(item.asItem(),2),1F, 100)
                .group(group)
                .unlockedBy("has", this.has(cluster))
                .save(this.output, getItemName(item) + "_blasting_from_cluster");
    }


    private void plateRecipe(ItemLike plate, TagKey<Item> ingotTag){
        shaped(RecipeCategory.MISC,plate,3)
                .pattern("NNN")
                .define('N', ingotTag)
                .unlockedBy("has", has(ingotTag))
                .save(output);
    }


    private void buildFocalManipulatorRecipe() {
        arcaneShaped(new ItemStackTemplate(TCItems.FOCAL_MANIPULATOR), 100)
                .aspect(TCAspects.TERRA, 1)
                .aspect(TCAspects.AQUA, 1)
                .pattern("ISI")
                .pattern("BRB")
                .pattern("GTG")
                .define('I', Items.IRON_INGOT)
                .define('S', TCItems.STONE_ARCANE)
                .define('B', TCItems.STONE_ARCANE)
                .define('R', TCItems.VIS_RESONATOR)
                .define('G', Items.GOLD_INGOT)
                .define('T', TCItems.RESEARCH_TABLE)
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);
    }

    private void buildInfusionAltarRecipes() {
        arcaneShaped(new ItemStackTemplate(TCItems.INFUSION_MATRIX), 150)
                .allAspects()
                .pattern("S S")
                .pattern(" N ")
                .pattern("S S")
                .define('S', TCItems.STONE_ARCANE_BRICK)
                .define('N', TCItemTags.NITORS)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_infusion"), Optional.of(2), false))
                .unlockedBy("has", has(TCItems.STONE_ARCANE_BRICK))
                .save(output);

        new InfusionRecipeBuilder(registries.lookupOrThrow(IAspect.REGISTRY_KEY), RecipeCategory.MISC,
                new ItemStackTemplate(TCItems.INGOT_THAUMIUM), Ingredient.of(Items.IRON_INGOT))
                .component(Ingredient.of(TCItems.AMBER.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(TCItems.AMBER.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .aspect(TCAspects.METALLUM, 8)
                .aspect(TCAspects.PRAECANTATIO, 8)
                .instability(2)
                .unlockedBy("has", has(Items.IRON_INGOT))
                .save(output, TCIds.MODID + ":infusion/test_thaumium");

        arcaneShaped(new ItemStackTemplate(TCItems.PEDESTAL_ARCANE), 10)
                .pattern("SSS")
                .pattern(" B ")
                .pattern("SSS")
                .define('S', TCItems.STONE_ARCANE)
                .define('B', TCItems.STONE_ARCANE)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_infusion"), Optional.of(1), false))
                .unlockedBy("has", has(TCItems.STONE_ARCANE))
                .save(output);
    }

    private void buildGearRecipes() {
        toolRecipes("thaumium", TCItemTags.INGOTS_THAUMIUM,
                TCItems.THAUMIUM_SWORD.get(), TCItems.THAUMIUM_PICKAXE.get(), TCItems.THAUMIUM_AXE.get(),
                TCItems.THAUMIUM_SHOVEL.get(), TCItems.THAUMIUM_HOE.get());
        toolRecipes("void", TCItemTags.INGOTS_VOID_METAL,
                TCItems.VOID_SWORD.get(), TCItems.VOID_PICKAXE.get(), TCItems.VOID_AXE.get(),
                TCItems.VOID_SHOVEL.get(), TCItems.VOID_HOE.get());
        armorRecipes("thaumium", TCItemTags.INGOTS_THAUMIUM,
                TCItems.THAUMIUM_HELM.get(), TCItems.THAUMIUM_CHEST.get(),
                TCItems.THAUMIUM_LEGS.get(), TCItems.THAUMIUM_BOOTS.get());
        armorRecipes("void", TCItemTags.INGOTS_VOID_METAL,
                TCItems.VOID_HELM.get(), TCItems.VOID_CHEST.get(),
                TCItems.VOID_LEGS.get(), TCItems.VOID_BOOTS.get());
        robeDyeRecipe(TCItems.CLOTH_CHEST.get());
        robeDyeRecipe(TCItems.CLOTH_LEGS.get());
        robeDyeRecipe(TCItems.CLOTH_BOOTS.get());
    }

    private void robeDyeRecipe(Item target) {
        CustomCraftingRecipeBuilder.customCrafting(RecipeCategory.MISC,
                        (commonInfo, bookInfo) -> new DyeRecipe(commonInfo, bookInfo,
                                Ingredient.of(target), tag(ItemTags.DYES), new ItemStackTemplate(target)))
                .unlockedBy(getHasName(target), has(target))
                .group("cloth_robes")
                .save(output, TCIds.MODID + ":" + BuiltInRegistries.ITEM.getKey(target).getPath() + "_dyed");
    }

    private void toolRecipes(String name, TagKey<Item> ingot, Item sword, Item pickaxe, Item axe, Item shovel, Item hoe) {
        shaped(RecipeCategory.COMBAT, sword)
                .pattern("I").pattern("I").pattern("S")
                .define('I', ingot).define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.TOOLS, pickaxe)
                .pattern("III").pattern(" S ").pattern(" S ")
                .define('I', ingot).define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.TOOLS, axe)
                .pattern("II").pattern("IS").pattern(" S")
                .define('I', ingot).define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.TOOLS, shovel)
                .pattern("I").pattern("S").pattern("S")
                .define('I', ingot).define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.TOOLS, hoe)
                .pattern("II").pattern(" S").pattern(" S")
                .define('I', ingot).define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_ingot", has(ingot)).save(output);
    }

    private void armorRecipes(String name, TagKey<Item> ingot, Item helm, Item chest, Item legs, Item boots) {
        shaped(RecipeCategory.COMBAT, helm)
                .pattern("III").pattern("I I")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.COMBAT, chest)
                .pattern("I I").pattern("III").pattern("III")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.COMBAT, legs)
                .pattern("III").pattern("I I").pattern("I I")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot)).save(output);
        shaped(RecipeCategory.COMBAT, boots)
                .pattern("I I").pattern("I I")
                .define('I', ingot)
                .unlockedBy("has_ingot", has(ingot)).save(output);
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




    private void buildFocusRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStackTemplate(TCItems.FOCUS_1.get()),
                DataComponentIngredient.of(TCDataComponents.CRYSTAL_ASPECT.get(),
                        new AspectInstance(aspects.getOrThrow(TCAspects.ORDO), 1), TCItems.ESSENTIA_CRYSTAL.get()))
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_auromancy"), Optional.of(1), false))
                .aspect(TCAspects.VITREUS, 20)
                .aspect(TCAspects.PRAECANTATIO, 10)
                .aspect(TCAspects.AURAM, 5)
                .unlockedBy("has", has(TCItems.ESSENTIA_CRYSTAL.get()))
                .save(output);

        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStackTemplate(TCItems.FOCUS_2.get()),
                Ingredient.of(TCItems.FOCUS_1.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(items.getOrThrow(Tags.Items.GEMS_DIAMOND)))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(Items.ENDER_PEARL))
                .aspect(TCAspects.PRAECANTATIO, 25)
                .aspect(TCAspects.ORDO, 50)
                .instability(3)
                .unlockedBy("has", has(TCItems.FOCUS_1.get()))
                .save(output);

        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC, new ItemStackTemplate(TCItems.FOCUS_3.get()),
                Ingredient.of(TCItems.FOCUS_2.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(TCItems.PRIMORDIAL_PEARL.get()))
                .component(Ingredient.of(TCItems.QUICKSILVER.get()))
                .component(Ingredient.of(Items.NETHER_STAR))
                .aspect(TCAspects.PRAECANTATIO, 25)
                .aspect(TCAspects.ORDO, 50)
                .aspect(TCAspects.VACUOS, 100)
                .instability(5)
                .unlockedBy("has", has(TCItems.FOCUS_2.get()))
                .save(output);
    }

    private void buildCrucibleRecipes() {
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        new CrucibleRecipeBuilder(aspects,RecipeCategory.MISC, new ItemStackTemplate(TCItems.TALLOW.get()), Ingredient.of(Items.ROTTEN_FLESH))
                .aspect(TCAspects.IGNIS, 1)
                .unlockedBy("has", has(Items.ROTTEN_FLESH))
                .save(output);

        new CrucibleRecipeBuilder(aspects,RecipeCategory.MISC, new ItemStackTemplate(TCItems.NITORS.get(DyeColor.YELLOW).get()), Ingredient.of(Items.GLOWSTONE_DUST))
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_alchemy"), Optional.of(1), false))
                .aspect(TCAspects.POTENTIA, 10)
                .aspect(TCAspects.IGNIS, 10)
                .aspect(TCAspects.LUX, 10)
                .unlockedBy("has", has(Items.GLOWSTONE_DUST))
                .save(output);

        registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspect -> {
            new CrucibleRecipeBuilder(aspects,RecipeCategory.MISC, new ItemStackTemplate(TCItems.ESSENTIA_CRYSTAL, 1, DataComponentPatch.builder().set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1)).build()),
                    Ingredient.of(items.getOrThrow(TCItemTags.NUGGETS_QUARTZ)))
                    .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_alchemy"), Optional.of(1), false))
                    .aspect(aspect, 2)
                    .unlockedBy("has", has(TCItemTags.NUGGETS_QUARTZ))
                    .save(output, TCIds.MODID + ":crucible/vis_crystal/" + aspect.getKey().identifier().getPath());
        });

        new CrucibleRecipeBuilder(aspects,RecipeCategory.MISC, new ItemStackTemplate(TCItems.INGOT_VOID.get()), Ingredient.of(TCItems.VOID_SEED.get()))
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_alchemy"), Optional.of(1), false))
                .aspect(TCAspects.METALLUM, 10)
                .aspect(TCAspects.VITIUM, 5)
                .unlockedBy("has", has(TCItems.VOID_SEED.get()))
                .save(output, TCIds.MODID + ":crucible/void_ingot");

        clusterRecipe(TCItems.CLUSTER_IRON,Tags.Items.ORES_IRON);
        clusterRecipe(TCItems.CLUSTER_GOLD,Tags.Items.ORES_GOLD);
        clusterRecipe(TCItems.CLUSTER_COPPER,Tags.Items.ORES_COPPER);
        clusterRecipe(TCItems.CLUSTER_TIN,TCItemTags.ORES_TIN);
        clusterRecipe(TCItems.CLUSTER_SILVER,TCItemTags.ORES_SILVER);
        clusterRecipe(TCItems.CLUSTER_LEAD,TCItemTags.ORES_LEAD);
        clusterRecipe(TCItems.CLUSTER_CINNABAR,TCItemTags.ORES_CINNABAR);
        clusterRecipe(TCItems.CLUSTER_QUARTZ,Tags.Items.ORES_QUARTZ);
    }

    private void clusterRecipe(ItemLike cluster, TagKey<Item> oreTag){
        HolderLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        new CrucibleRecipeBuilder(aspects,RecipeCategory.MISC,new ItemStackTemplate(cluster.asItem()),Ingredient.of(items.getOrThrow(oreTag)))
                .aspect(TCAspects.METALLUM,5)
                .aspect(TCAspects.ORDO,5)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "metal_purification"), Optional.of(0), false))
                .unlockedBy("has", has(oreTag))
                .save(output.withConditions(new NotCondition(new TagEmptyCondition<>(oreTag))));
    }



    private void buildArcaneWorkbenchRecipes() {
        arcaneShaped(new ItemStackTemplate(TCItems.THAUMOMETER), 20)
                .allAspects()
                .pattern(" G ")
                .pattern("GPG")
                .pattern(" G ")
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('P', Tags.Items.GLASS_PANES)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"first_steps"),Optional.of(1),false))
                .unlockedBy("has", has(Tags.Items.INGOTS_GOLD))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.CASTER_BASIC), 100)
                .allAspects()
                .pattern("III")
                .pattern("LRL")
                .pattern("LTL")
                .define('I', TCItemTags.PLATES_IRON)
                .define('L', Items.LEATHER)
                .define('R', TCItems.VIS_RESONATOR)
                .define('T', TCItems.THAUMOMETER)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_auromancy"), Optional.of(2), false))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.VIS_RESONATOR), 50)
                .aspect(TCAspects.AER)
                .aspect(TCAspects.AQUA)
                .requires(TCItemTags.PLATES_IRON)
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
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"workbench_charger"),Optional.of(1),false))
                .unlockedBy("has", has(TCItems.VIS_RESONATOR))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.GOGGLES_REVEALING),50)
                .pattern("LBL")
                .pattern("L L")
                .pattern("MBM")
                .define('L',Tags.Items.LEATHERS)
                .define('B',TCItemTags.INGOTS_BRASS)
                .define('M',TCItems.THAUMOMETER)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"unlock_artifice"),Optional.of(3),false))
                .unlockedBy("has",has(TCItems.THAUMOMETER))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.ALEMBIC),50)
                .aspect(TCAspects.AQUA)
                .pattern("GFG")
                .pattern("PBP")
                .pattern("GFG")
                .define('G',TCItems.PLANK_GREATWOOD)
                .define('F',Items.HOPPER)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('B',Items.BUCKET)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"essentia_smelter"),Optional.of(1),false))
                .unlockedBy("has",has(TCItemTags.PLATES_BRASS))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.SMELTER_BASIC),50)
                .aspect(TCAspects.IGNIS)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', ItemTags.STONE_TOOL_MATERIALS)
                .define('F',Items.FURNACE)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('R',TCItems.CRUCIBLE)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"essentia_smelter"),Optional.of(3),false))
                .unlockedBy("has",has(TCItems.CRUCIBLE))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.SMELTER_THAUMIUM),250)
                .aspect(TCAspects.IGNIS,2)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', TCItemTags.PLATES_THAUMIUM)
                .define('F',Items.FURNACE)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('R',TCItems.SMELTER_BASIC)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"thaumium_essentia_smelter"),Optional.of(1),false))
                .unlockedBy("has",has(TCItems.SMELTER_BASIC))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.SMELTER_VOID),750)
                .aspect(TCAspects.IGNIS,3)
                .pattern("PRP")
                .pattern("CFC")
                .pattern("CCC")
                .define('C', TCItemTags.PLATES_VOID_METAL)
                .define('F',Items.FURNACE)
                .define('P', TCItemTags.PLATES_BRASS)
                .define('R',TCItems.SMELTER_THAUMIUM)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"void_essentia_smelter"),Optional.of(1),false))
                .unlockedBy("has",has(TCItems.SMELTER_THAUMIUM))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.JAR_NORMAL),5)
                .pattern("PRP")
                .pattern("P P")
                .pattern("PPP")
                .define('P', Tags.Items.GLASS_PANES)
                .define('R',ItemTags.WOODEN_SLABS)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"warded_jars"),Optional.of(0),false))
                .unlockedBy("has",has(Tags.Items.GLASS_PANES))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.JAR_VOID),50)
                .aspect(TCAspects.PERDITIO)
                .requires(TCItems.JAR_NORMAL)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"warded_jars"),Optional.of(0),false))
                .unlockedBy("has",has(TCItems.JAR_NORMAL))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.TUBE,8),10)
                .pattern(" Q ")
                .pattern("PGP")
                .pattern(" B ")
                .define('Q', TCItemTags.NUGGETS_QUICKSILVER)
                .define('P', TCItemTags.PLATES_IRON)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', TCItemTags.NUGGETS_BRASS)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"tubes"),Optional.of(1),false))
                .unlockedBy("has",has(Tags.Items.GEMS_QUARTZ))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.TUBE_RESTRICT),10)
                .aspect(TCAspects.TERRA)
                .requires(TCItems.TUBE)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"tubes"),Optional.of(0),false))
                .unlockedBy("has",has(TCItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.TUBE_ONEWAY),10)
                .aspect(TCAspects.AQUA)
                .requires(TCItems.TUBE)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"tubes"),Optional.of(0),false))
                .unlockedBy("has",has(TCItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.TUBE_FILTER),10)
                .requires(TCItems.TUBE)
                .requires(Items.HOPPER)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"tubes"),Optional.of(0),false))
                .unlockedBy("has",has(TCItems.TUBE))
                .save(output);

        arcaneShapeless(new ItemStackTemplate(TCItems.TUBE_VALVE),10)
                .requires(TCItems.TUBE)
                .requires(Items.LEVER)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"tubes"),Optional.of(0),false))
                .unlockedBy("has",has(TCItems.TUBE))
                .save(output);

        arcaneShaped(new ItemStackTemplate(TCItems.TUBE_BUFFER),25)
                .pattern("PVP")
                .pattern("TIT")
                .pattern("PRP")
                .define('P',TCItems.PHIAL)
                .define('V',TCItems.TUBE_VALVE)
                .define('T',TCItems.TUBE)
                .define('I',TCItemTags.PLATES_IRON)
                .define('R',TCItems.TUBE_RESTRICT)
                .gate(new ResearchGate(Identifier.fromNamespaceAndPath(TCIds.MODID,"tubes"),Optional.of(0),false))
                .unlockedBy("has",has(TCItems.TUBE))
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
