package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundClearResearchFlagsHandler {
    private ServerboundClearResearchFlagsHandler() {}

    public static void handle(ServerboundClearResearchFlagsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
            if (!knowledge.isResearchKnown(payload.research())) return;
            boolean changed = false;
            for (ResearchFlag flag : payload.flags()) {
                if (knowledge.clearResearchFlag(payload.research(), flag)) {
                    changed = true;
                }
            }
            if (changed) knowledge.sync(player);
        });
    }
}
