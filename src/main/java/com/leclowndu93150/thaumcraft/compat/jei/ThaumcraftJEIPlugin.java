package com.leclowndu93150.thaumcraft.compat.jei;

import com.leclowndu93150.thaumcraft.api.aspect.AspectIndexAccess;
import java.util.List;
import java.util.ArrayList;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import com.leclowndu93150.thaumcraft.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumcraft.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumcraft.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.Thaumcraft;
import com.leclowndu93150.thaumcraft.api.aspect.*;
import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGated;
import com.leclowndu93150.thaumcraft.client.recipes.TCClientRecipes;
import com.leclowndu93150.thaumcraft.compat.jei.category.*;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientHelper;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumcraft.config.ThaumcraftClientConfig;
import com.leclowndu93150.thaumcraft.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumcraft.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCMenus;
import com.leclowndu93150.thaumcraft.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.leclowndu93150.thaumcraft.client.screen.casters.FocalManipulatorScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPools;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@JeiPlugin
public final class ThaumcraftJEIPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(TCIds.MODID, "jei_plugin");

    /*public static Map<IRecipeType<?>,List<RecipeHolder<?>>> searchAffectedRecipes;
    public static IJeiRuntime runtime;*/

    public ThaumcraftJEIPlugin() {}

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        ISubtypeInterpreter<ItemStack> aspectsInterpreter = ThaumcraftJEIPlugin::aspectsSubtype;
        ISubtypeInterpreter<ItemStack> essentiaInterpreter = ThaumcraftJEIPlugin::essentiaSubtype;
        ISubtypeInterpreter<ItemStack> filterInterpreter = ThaumcraftJEIPlugin::aspectFilterSubtype;
        ISubtypeInterpreter<ItemStack> crystalAspectInterpreter = ThaumcraftJEIPlugin::crystalAspectSubtype;
        registration.registerSubtypeInterpreter(TCItems.JAR_NORMAL.get(), essentiaInterpreter);
        registration.registerSubtypeInterpreter(TCItems.JAR_VOID.get(), essentiaInterpreter);
        registration.registerSubtypeInterpreter(TCItems.LABEL.get(), filterInterpreter);
        registration.registerSubtypeInterpreter(TCItems.TUBE_FILTER.get(), filterInterpreter);
        registration.registerSubtypeInterpreter(TCItems.SALIS_MUNDUS.get(), aspectsInterpreter);
        registration.registerSubtypeInterpreter(TCItems.ESSENTIA_CRYSTAL.get(), crystalAspectInterpreter);
        registration.registerSubtypeInterpreter(TCItems.PHIAL.get(), aspectsInterpreter);
        registration.registerFromDataComponentTypes(TCItems.CELESTIAL_NOTES.asItem(), TCDataComponents.CELESTIAL_BODY.get());
        registration.registerFromDataComponentTypes(TCItems.RESEARCH_NOTE.get(), TCDataComponents.RESEARCH_NOTE.get());
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        RegistryAccess registryAccess = clientRegistryAccess();
        if (registryAccess == null) {
            return;
        }
        List<ItemStack> notes = new ArrayList<>();
        for (Holder.Reference<IResearchEntry> holder
                : registryAccess.lookupOrThrow(IResearchEntry.REGISTRY_KEY).listElements().toList()) {
            IResearchEntry entry = holder.value();
            int theoryRows = ResearchNotes.theoryRowsBefore(entry, entry.stages().size());
            if (theoryRows == 0) {
                continue;
            }
            Identifier entryId = holder.key().identifier();
            AspectList anchors = ResearchNotes.anchors(registryAccess, entry);
            int color = NoteGenerator.primaryColor(anchors);
            for (int ordinal = 0; ordinal < theoryRows; ordinal++) {
                notes.add(displayNote(entryId, ordinal, color, false));
                notes.add(displayNote(entryId, ordinal, color, true));
            }
        }
        registration.addExtraItemStacks(notes);
    }

    private static ItemStack displayNote(Identifier entry, int ordinal, int color, boolean complete) {
        ItemStack stack = new ItemStack(TCItems.RESEARCH_NOTE.get());
        stack.set(TCDataComponents.RESEARCH_NOTE.get(),
                new ResearchNoteData(entry, ordinal, color, complete, 0, List.of()));
        if (complete) {
            stack.set(TCDataComponents.NOTE_COMPLETE.get(), true);
        }
        return stack;
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        RegistryAccess registryAccess = clientRegistryAccess();
        if (registryAccess == null) {
            return;
        }
        Optional<Registry<IAspect>> registryOpt = registryAccess.lookup(IAspect.REGISTRY_KEY);
        if (registryOpt.isEmpty()) {
            return;
        }
        Registry<IAspect> registry = registryOpt.get();
        Player player = Minecraft.getInstance().player;
        List<Holder<IAspect>> all = new ArrayList<>(registry.size());
        registry.listElements()
                .filter(ref -> player != null && AspectPools.isDiscovered(player, ref))
                .forEach(all::add);
        registration.register(
                AspectIngredientType.INSTANCE,
                all.stream().sorted(Comparator.comparingInt(e->clientRegistryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getId(e.getKey()))).sorted(Comparator.comparing(h->!h.value().isPrimal())).map(aspect->new AspectInstance(aspect,1)).toList(),
                AspectIngredientHelper.INSTANCE,
                AspectIngredientRenderer.INSTANCE,
                AspectInstance.CODEC
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(FocalManipulatorScreen.class, new FocalManipulatorGuiHandler());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers helpers = registration.getJeiHelpers();
        List<IRecipeCategory<?>> categories = new ArrayList<>(2);
        categories.add(new ArcaneWorkbenchCategory(helpers.getGuiHelper()));
        categories.add(new CrucibleCategory(helpers.getGuiHelper()));
        categories.add(new InfusionCategory<>(helpers.getGuiHelper(), InfusionCategory.RECIPE_TYPE, "recipe.type.infusion"));
        categories.add(new InfusionCategory<>(helpers.getGuiHelper(), InfusionCategory.ENCHANTMENT_RECIPE_TYPE, "recipe.type.infusion_enchantment"));
        categories.add(new DustTriggerCategory(helpers.getGuiHelper()));
        categories.add(new AspectCompositionCategory(helpers.getGuiHelper(), pickIconAspect()));
        categories.add(new AspectFromStacksCategory(helpers.getGuiHelper()));
        categories.add(new MultiblockCategory(helpers.getGuiHelper()));
        registration.addRecipeCategories(categories.toArray(new IRecipeCategory<?>[0]));
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(SalisMundusRecipe.class, SalisMundusCraftingExtension.INSTANCE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        addTypedRecipes(registration,ArcaneWorkbenchCategory.RECIPE_TYPE,TCRecipeTypes.ARCANE.get(), null);
        addTypedRecipes(registration,CrucibleCategory.RECIPE_TYPE,TCRecipeTypes.CRUCIBLE.get(), null);
        addTypedRecipes(registration,InfusionCategory.RECIPE_TYPE,TCRecipeTypes.INFUSION.get(), null);
        addTypedRecipes(registration,InfusionCategory.ENCHANTMENT_RECIPE_TYPE,TCRecipeTypes.INFUSION_ENCHANTMENT.get(), null);
        registerAspectCompositions(registration);
        addTypedRecipes(registration,DustTriggerCategory.RECIPE_TYPE,TCRecipeTypes.DUST_TRIGGER.get(), r->r.value() instanceof DustTriggerSimpleRecipe || r.value() instanceof DustTriggerTagRecipe);
        addTypedRecipes(registration,MultiblockCategory.RECIPE_TYPE,TCRecipeTypes.DUST_TRIGGER.get(), r->r.value() instanceof DustTriggerMultiblockRecipe);
        registerAspectInfoPages(registration);
        registerAspectFromStacksPages(registration);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        AspectJeiSync.onRuntimeAvailable(jeiRuntime);
    }

  /*  @Override
    public  void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (ThaumcraftClientConfig.hideRecipesIfMissingResearch()) hideUnresearchedRecipes(jeiRuntime);
    }

    private <I extends RecipeInput, R extends Recipe<I> & ResearchGated> void hideUnresearchedRecipes(IJeiRuntime runtime){
        searchAffectedRecipes = new HashMap<>();
        for (RecipeType<@NonNull R> type : new RecipeType[]{TCRecipeTypes.DUST_TRIGGER.get(),TCRecipeTypes.CRUCIBLE.get()}) {
            RecipeMap recipes = TCClientRecipes.getRecipeMapForType(Minecraft.getInstance().level, type);
            List<RecipeHolder<@NonNull R>> holders = List.copyOf(recipes.byType(type));
            Identifier uid = BuiltInRegistries.RECIPE_TYPE.getKey(type);
            List<RecipeHolder<@NonNull R>> hided = holders.stream().filter(r->!r.value().doesPassGate(Minecraft.getInstance().player)).toList();
            IRecipeHolderType<@NonNull R> jeiType = (IRecipeHolderType<R>) runtime.getRecipeManager().getRecipeType(uid).get();
            runtime.getRecipeManager().hideRecipes(jeiType,hided);
            searchAffectedRecipes.put(jeiType,List.copyOf(holders));
        }
    }*/

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RecipeTypes.CRAFTING, TCItems.ARCANE_WORKBENCH.get());
        registration.addCraftingStation(ArcaneWorkbenchCategory.RECIPE_TYPE, TCItems.ARCANE_WORKBENCH.get());
        registration.addCraftingStation(InfusionCategory.RECIPE_TYPE, TCItems.INFUSION_MATRIX.get());
        registration.addCraftingStation(InfusionCategory.ENCHANTMENT_RECIPE_TYPE, TCItems.INFUSION_MATRIX.get());
        registration.addCraftingStation(DustTriggerCategory.RECIPE_TYPE, TCItems.SALIS_MUNDUS.get());
        registration.addCraftingStation(MultiblockCategory.RECIPE_TYPE, TCItems.SALIS_MUNDUS.get());
        registration.addCraftingStation(CrucibleCategory.RECIPE_TYPE, TCItems.CRUCIBLE.get());
        registration.addCraftingStation(AspectCompositionCategory.RECIPE_TYPE, TCItems.THAUMONOMICON.get());
        registration.addCraftingStation(AspectFromStacksCategory.RECIPE_TYPE, TCItems.THAUMONOMICON.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(MenuArcaneWorkbench.class, TCMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchCategory.RECIPE_TYPE,1,15,16,36);
        registration.addRecipeTransferHandler(MenuArcaneWorkbench.class, TCMenus.ARCANE_WORKBENCH.get(), RecipeTypes.CRAFTING,1,9,16,36);
    }

    private static void registerAspectCompositions(IRecipeRegistration registration) {
        RegistryAccess registryAccess = clientRegistryAccess();
        if (registryAccess == null) {
            return;
        }
        Optional<Registry<IAspect>> registryOpt = registryAccess.lookup(IAspect.REGISTRY_KEY);
        if (registryOpt.isEmpty()) {
            return;
        }
        Registry<IAspect> registry = registryOpt.get();
        List<AspectCompositionCategory.Composition> compositions =
                AspectCompositionCategory.collect(registry.listElements().toList());
        registration.addRecipes(AspectCompositionCategory.RECIPE_TYPE, compositions);
    }

    private static void registerAspectInfoPages(IRecipeRegistration registration) {
        RegistryAccess registryAccess = clientRegistryAccess();
        if (registryAccess == null) {
            return;
        }
        Optional<Registry<IAspect>> registryOpt = registryAccess.lookup(IAspect.REGISTRY_KEY);
        if (registryOpt.isEmpty()) {
            return;
        }
        Registry<IAspect> registry = registryOpt.get();
        registry.listElements().forEach(holder -> {
            Component description = AspectComponents.description(holder);
            registration.addIngredientInfo(new AspectInstance(holder,1), AspectIngredientType.INSTANCE, description);
        });
    }


    private static void registerAspectFromStacksPages(IRecipeRegistration registration) {
        List<AspectFromStacksCategory.Wrapper> wrappers = new ArrayList<>();
        Map<ItemStack, AspectList> index = new HashMap<>();
        Map<Holder<IAspect>, List<ItemStack>> invertedIndex = new HashMap<>();

        registration.getIngredientManager()
                .getAllIngredients(VanillaTypes.ITEM_STACK)
                .forEach(stack -> {
                    AspectList aspectList = AspectIndexAccess.index().of(stack);
                    index.put(stack,aspectList);
                    aspectList.entries().forEach(instance -> {
                        invertedIndex
                                .computeIfAbsent(instance.aspect(), k -> new ArrayList<>())
                                .add(stack);
                    });
                });

        invertedIndex.entrySet().stream().sorted(Comparator.comparingInt(e->clientRegistryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getId(e.getKey().getKey()))).sorted(Comparator.comparing(e->!clientRegistryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(e.getKey().getKey()).value().isPrimal())).forEach((e)->{
            Holder<IAspect> aspect = e.getKey();
            List<ItemStack> stacks = e.getValue().stream()
                    .map(it-> it.copyWithCount(index.get(it).amountOf(aspect)))
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .sorted(Comparator.comparing(ItemStack::getCount).reversed())
                    .toList();

            int start = 0;
            while (start < stacks.size()){
                wrappers.add(new AspectFromStacksCategory.Wrapper(aspect,stacks.subList(start,Math.min(start+36,stacks.size()))));
                start+=36;
            }
        });

        registration.addRecipes(AspectFromStacksCategory.RECIPE_TYPE,wrappers);

    }


    private static Holder<IAspect> pickIconAspect() {
        RegistryAccess registryAccess = clientRegistryAccess();
        if (registryAccess != null) {
            Optional<Registry<IAspect>> registryOpt = registryAccess.lookup(IAspect.REGISTRY_KEY);
            if (registryOpt.isPresent()) {
                Registry<IAspect> registry = registryOpt.get();
                Optional<Holder.Reference<IAspect>> stable = registry.get(TCAspects.PRAECANTATIO);
                if (stable.isPresent()) {
                    return stable.get();
                }
                Optional<Holder.Reference<IAspect>> first = registry.listElements().findFirst();
                if (first.isPresent()) {
                    return first.get();
                }
            }
        }
        return null;
    }

    private <I extends RecipeInput, R extends Recipe<I>> void addTypedRecipes(IRecipeRegistration registration, IRecipeType<RecipeHolder<R>> type, RecipeType<R> vanillaType, @Nullable Predicate<RecipeHolder<R>> filter){
        RecipeMap recipes = TCClientRecipes.getRecipeMapForType(Minecraft.getInstance().level, vanillaType);
        List<RecipeHolder<R>> holders = List.copyOf(recipes.byType(vanillaType));
        if (filter != null) {
            holders = holders.stream().filter(filter).toList();
        }
        registration.addRecipes(type, holders);
    }

    public static RegistryAccess clientRegistryAccess() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level != null) {
            return level.registryAccess();
        }
        Thaumcraft.LOGGER.debug("JEI plugin: client level not available, deferring aspect-dependent registration");
        return null;
    }

    private static @Nullable Object aspectsSubtype(ItemStack stack, UidContext context) {
        AspectList list = stack.get(TCDataComponents.ASPECTS.get());
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list;
    }

    private static @Nullable Object essentiaSubtype(ItemStack stack, UidContext context) {
        return stack.get(TCDataComponents.ESSENTIA_CONTENTS.get());
    }

    private static @Nullable Object aspectFilterSubtype(ItemStack stack, UidContext context) {
        return stack.get(TCDataComponents.ASPECT_FILTER.get());
    }

    private static @Nullable Object crystalAspectSubtype(ItemStack stack, UidContext context) {
        return stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
    }
}
