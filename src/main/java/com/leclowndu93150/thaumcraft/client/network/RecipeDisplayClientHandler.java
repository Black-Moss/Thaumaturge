package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumcraft.client.screen.research.EntryDetailScreen;
import com.leclowndu93150.thaumcraft.network.ClientboundItemRecipePayload;
import com.leclowndu93150.thaumcraft.network.ClientboundRecipeDisplayPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RecipeDisplayClientHandler {
    private RecipeDisplayClientHandler() {}

    public static void handle(ClientboundRecipeDisplayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> RecipeDisplayCache.put(payload.recipeId(), payload.displays()));
    }

    public static void handleItemRecipe(ClientboundItemRecipePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            RecipeDisplayCache.put(payload.recipeId(), payload.displays());
            if (Minecraft.getInstance().screen instanceof EntryDetailScreen screen) {
                screen.openRecipeFromNavigation(payload.recipeId());
            }
        });
    }
}
