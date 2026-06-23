package com.leclowndu93150.thaumcraft.network;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundRecipeDisplayHandler {
    private ServerboundRecipeDisplayHandler() {}

    public static void handle(ServerboundRequestRecipeDisplayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            RecipeManager manager = level.recipeAccess();
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, payload.recipeId());
            manager.byKey(key).ifPresent(holder -> {
                List<RecipeDisplay> displays = holder.value().display();
                PacketDistributor.sendToPlayer(player, new ClientboundRecipeDisplayPayload(payload.recipeId(), displays));
            });
        });
    }
}
