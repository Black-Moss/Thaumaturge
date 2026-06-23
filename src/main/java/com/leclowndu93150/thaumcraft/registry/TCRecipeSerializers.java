package com.leclowndu93150.thaumcraft.registry;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.dust.DustTriggerTagRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TCIds.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustTriggerSimpleRecipe>> DUST_TRIGGER_SIMPLE =
            RECIPE_SERIALIZERS.register("dust_trigger_simple", () -> DustTriggerSimpleRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustTriggerTagRecipe>> DUST_TRIGGER_TAG =
            RECIPE_SERIALIZERS.register("dust_trigger_tag", () -> DustTriggerTagRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DustTriggerMultiblockRecipe>> DUST_TRIGGER_MULTIBLOCK =
            RECIPE_SERIALIZERS.register("dust_trigger_multiblock", () -> DustTriggerMultiblockRecipe.SERIALIZER);

    private TCRecipeSerializers() {}

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
