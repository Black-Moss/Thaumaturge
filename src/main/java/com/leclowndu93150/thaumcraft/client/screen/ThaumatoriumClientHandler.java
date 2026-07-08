package com.leclowndu93150.thaumcraft.client.screen;

import com.leclowndu93150.thaumcraft.content.essentia.thaumatorium.MenuThaumatorium;
import com.leclowndu93150.thaumcraft.network.ClientboundThaumatoriumRecipesPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ThaumatoriumClientHandler {
    private ThaumatoriumClientHandler() {}

    public static void handle(ClientboundThaumatoriumRecipesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
                    && mc.player.containerMenu instanceof MenuThaumatorium menu
                    && menu.containerId == payload.containerId()) {
                menu.clientRecipes = payload.entries();
            }
        });
    }
}
