package com.leclowndu93150.thaumcraft.client.recipes;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.Thaumcraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public class TCClientRecipes {

    private static RecipeMap recipeMap = RecipeMap.EMPTY;
    private static final Set<RecipeType<?>> knownRecipeTypes = Collections.newSetFromMap(new IdentityHashMap<>());

    @SubscribeEvent
    public static void receiveRecipes(RecipesReceivedEvent event){
        recipeMap = event.getRecipeMap();
        knownRecipeTypes.clear();
        knownRecipeTypes.addAll(event.getRecipeTypes());
    }

    public static RecipeMap getRecipeMapForType(Level level, RecipeType<?> recipeType) {
        if (level instanceof ClientLevel) {
            if (!knownRecipeTypes.contains(recipeType)) {
                Thaumcraft.LOGGER.warn("Recipes of type {} haven't been received yet.", recipeType);
                return RecipeMap.EMPTY;
            }

            return recipeMap;
        }

        return RecipeMap.EMPTY;
    }
}
