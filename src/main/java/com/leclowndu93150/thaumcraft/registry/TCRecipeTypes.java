package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import com.leclowndu93150.thaumcraft.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumcraft.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.stream.Collectors;

public final class TCRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, TCIds.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DustTrigger>> DUST_TRIGGER =
            RECIPE_TYPES.register("dust_trigger", () -> new RecipeType<DustTrigger>() {
                @Override
                public String toString() {
                    return "thaumcraft:dust_trigger";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionRecipe>> INFUSION =
            RECIPE_TYPES.register("infusion", () -> new RecipeType<InfusionRecipe>() {
                @Override
                public String toString() {
                    return "thaumcraft:infusion";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleRecipe>> CRUCIBLE =
            RECIPE_TYPES.register("crucible", () -> new RecipeType<CrucibleRecipe>() {
                @Override
                public String toString() {
                    return "thaumcraft:crucible";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<ArcaneCraftingRecipe>> ARCANE =
            RECIPE_TYPES.register("arcane_workbench", () -> new RecipeType<ArcaneCraftingRecipe>() {
                @Override
                public String toString() {
                    return "thaumcraft:arcane_workbench";
                }
            });

    private TCRecipeTypes() {}

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
    }

    public static void registerSynchronizedRecipes(OnDatapackSyncEvent event){
        List<RecipeType<?>> types = RECIPE_TYPES.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toUnmodifiableList());
        event.sendRecipes(types);
    }
}
