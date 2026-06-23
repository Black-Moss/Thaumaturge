package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundUnlockResearchHandler {
    private ServerboundUnlockResearchHandler() {}

    public static void handle(ServerboundUnlockResearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.unlock(player, payload.research());
            }
        });
    }
}
