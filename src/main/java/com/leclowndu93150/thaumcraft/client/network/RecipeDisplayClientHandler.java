package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumcraft.network.ClientboundRecipeDisplayPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RecipeDisplayClientHandler {
    private RecipeDisplayClientHandler() {}

    public static void handle(ClientboundRecipeDisplayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> RecipeDisplayCache.put(payload.recipeId(), payload.displays()));
    }
}
