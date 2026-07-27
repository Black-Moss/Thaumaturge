package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundAdvanceStageHandler {
    private ServerboundAdvanceStageHandler() {}

    public static void handle(ServerboundAdvanceStagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.advanceStage(player, payload.research());
            }
        });
    }
}
