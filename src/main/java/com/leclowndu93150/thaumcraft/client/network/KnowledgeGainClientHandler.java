package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.hud.KnowledgeGainOverlay;
import com.leclowndu93150.thaumcraft.network.ClientboundKnowledgeGainPayload;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public final class KnowledgeGainClientHandler {
    private static final int BASE_DURATION_TICKS = 40;
    private static final int EXTRA_DURATION_SPREAD = 20;

    private KnowledgeGainClientHandler() {}

    public static void handle(ClientboundKnowledgeGainPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.level == null) {
                return;
            }
            KnowledgeGainOverlay.addTracker(
                    payload.knowledgeType(),
                    payload.category().orElse(null),
                    BASE_DURATION_TICKS + mc.level.getRandom().nextInt(EXTRA_DURATION_SPREAD),
                    mc.level.getRandom().nextLong());
            mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    TCSounds.LEARN.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
        });
    }
}
