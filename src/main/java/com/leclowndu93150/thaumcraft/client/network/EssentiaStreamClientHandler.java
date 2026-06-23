package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.render.stream.EssentiaStreamManager;
import com.leclowndu93150.thaumcraft.network.ClientboundEssentiaStreamPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public final class EssentiaStreamClientHandler {
    private EssentiaStreamClientHandler() {}

    public static void handle(ClientboundEssentiaStreamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> EssentiaStreamManager.spawn(
                payload.sx(), payload.sy(), payload.sz(),
                payload.tx(), payload.ty(), payload.tz(),
                payload.color(), payload.count(), payload.scale(), payload.extend(), payload.my()));
    }
}
