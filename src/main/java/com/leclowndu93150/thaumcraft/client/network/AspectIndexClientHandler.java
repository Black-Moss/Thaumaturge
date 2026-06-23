package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumcraft.network.ClientboundAspectIndexPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AspectIndexClientHandler {
    private AspectIndexClientHandler() {}

    public static void handle(ClientboundAspectIndexPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> AspectIndexHolder.set(payload.index()));
    }
}
