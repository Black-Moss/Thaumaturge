package com.leclowndu93150.thaumcraft.compat.jei;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.Thaumcraft;
import com.leclowndu93150.thaumcraft.api.aspect.AspectComponents;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import com.leclowndu93150.thaumcraft.compat.jei.category.AspectCompositionCategory;
import com.leclowndu93150.thaumcraft.compat.jei.category.DustTriggerCategory;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientHelper;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import com.leclowndu93150.thaumcraft.registry.TCRecipeTypes;
import com.leclowndu93150.thaumcraft.registry.items.TCItemsHContainers;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

@JeiPlugin
public final class ThaumcraftJEIPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(TCIds.MODID, "jei_plugin");

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
        registration.registerSubtypeInterpreter(TCItemsHContainers.PHIAL.get(), aspectsInterpreter);
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
        List<Holder<IAspect>> all = new ArrayList<>(registry.size());
        registry.listElements().forEach(all::add);
        Codec<Holder<IAspect>> codec = RegistryFixedCodec.create(IAspect.REGISTRY_KEY);
        registration.register(
                AspectIngredientType.INSTANCE,
                all,
                AspectIngredientHelper.INSTANCE,
                AspectIngredientRenderer.INSTANCE,
                codec
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers helpers = registration.getJeiHelpers();
        List<IRecipeCategory<?>> categories = new ArrayList<>(2);
        categories.add(new DustTriggerCategory(helpers.getGuiHelper()));
        categories.add(new AspectCompositionCategory(helpers.getGuiHelper(), pickIconAspect()));
        registration.addRecipeCategories(categories.toArray(new IRecipeCategory<?>[0]));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registerAspectCompositions(registration);
        registerDustTriggers(registration);
        registerAspectInfoPages(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(DustTriggerCategory.RECIPE_TYPE, TCItems.SALIS_MUNDUS.get());
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

    private static void registerDustTriggers(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        if (!(level.recipeAccess() instanceof RecipeManager manager)) {
            return;
        }
        RecipeType<DustTrigger> type = TCRecipeTypes.DUST_TRIGGER.get();
        List<RecipeHolder<DustTrigger>> triggers = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value().getType() != type) {
                continue;
            }
            if (!(holder.value() instanceof DustTrigger)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            RecipeHolder<DustTrigger> cast = (RecipeHolder<DustTrigger>) holder;
            triggers.add(cast);
        }
        registration.addRecipes(DustTriggerCategory.RECIPE_TYPE, triggers);
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
            registration.addIngredientInfo(holder, AspectIngredientType.INSTANCE, description);
        });
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

    private static RegistryAccess clientRegistryAccess() {
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
