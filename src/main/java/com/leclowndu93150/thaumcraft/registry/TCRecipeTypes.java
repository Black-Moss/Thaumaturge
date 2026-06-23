package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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

    private TCRecipeTypes() {}

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
    }
}
