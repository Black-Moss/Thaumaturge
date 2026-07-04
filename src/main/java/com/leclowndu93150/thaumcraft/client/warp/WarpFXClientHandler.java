package com.leclowndu93150.thaumcraft.client.warp;

import com.leclowndu93150.thaumcraft.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class WarpFXClientHandler {
    private WarpFXClientHandler() {}

    public static void handle(ClientboundWarpFXPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            switch (payload.kind()) {
                case ClientboundWarpFXPayload.KIND_HEARTBEAT -> Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(TCSounds.HEARTBEAT.get(), 1.0F, 1.0F));
                case ClientboundWarpFXPayload.KIND_MIST -> WarpFogState.startMist();
                case ClientboundWarpFXPayload.KIND_MIST_SHORT -> WarpFogState.startShortMist();
                default -> {}
            }
        });
    }
}
