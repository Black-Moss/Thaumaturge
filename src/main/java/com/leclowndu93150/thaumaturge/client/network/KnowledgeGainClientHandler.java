package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.hud.KnowledgeGainOverlay;
import com.leclowndu93150.thaumaturge.network.ClientboundKnowledgeGainPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class KnowledgeGainClientHandler {
    private static final int BASE_DURATION_TICKS = 40;
    private static final int EXTRA_DURATION_SPREAD = 20;

    private KnowledgeGainClientHandler() {}

    public static void handle(ClientboundKnowledgeGainPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> addTrackers(payload));
    }

    private static void addTrackers(ClientboundKnowledgeGainPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (minecraft.player == null || level == null) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int point = 0; point < payload.count(); point++) {
            int duration = BASE_DURATION_TICKS + random.nextInt(EXTRA_DURATION_SPREAD);
            KnowledgeGainOverlay.addTracker(payload.knowledgeType(), payload.category().orElse(null), duration, random.nextLong());
        }
    }
}
