package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.content.research.theorycraft.ResearchTableData;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.TheorycraftManager;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundTheorycraftHandlers {
    private ServerboundTheorycraftHandlers() {}

    public static void handlePlayCard(ServerboundPlayCardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TheorycraftManager.playCard(player, payload.choiceIndex());
            }
        });
    }

    public static void handleEndSession(ServerboundEndSessionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TheorycraftManager.endSession(player);
            }
        });
    }

    public static void handleCardAnimationComplete(ServerboundCardAnimationCompletePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TheorycraftManager.completeCardAnimation(player);
            }
        });
    }

    public static void handleDrawCards(ServerboundDrawCardsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchTableData data = player.getData(TCAttachments.RESEARCH_TABLE);
                if (data.inspirationStart() <= 0) return;
                if (!data.cardChoices().isEmpty()) return;
                TheorycraftManager.drawHand(player, data, TheorycraftManager.DEFAULT_HAND_SIZE);
                data.sync(player);
            }
        });
    }
}
