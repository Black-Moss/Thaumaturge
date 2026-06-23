package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.render.stream.BoreStreamInstance;
import com.leclowndu93150.thaumcraft.client.render.stream.BoreVoidStreamManager;
import com.leclowndu93150.thaumcraft.client.render.stream.VoidStreamInstance;
import com.leclowndu93150.thaumcraft.network.ClientboundBoreStreamPayload;
import com.leclowndu93150.thaumcraft.network.ClientboundVoidStreamPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public final class BoreVoidStreamClientHandler {
    private BoreVoidStreamClientHandler() {}

    public static void handleBore(ClientboundBoreStreamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BoreVoidStreamManager.addBore(new BoreStreamInstance(
                payload.sx(), payload.sy(), payload.sz(),
                payload.targetEntityId(),
                payload.color(), payload.count(), payload.scale(), payload.extend(), payload.my())));
    }

    public static void handleVoid(ClientboundVoidStreamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BoreVoidStreamManager.addVoid(new VoidStreamInstance(
                payload.sx(), payload.sy(), payload.sz(),
                payload.tx(), payload.ty(), payload.tz(),
                payload.seed(), payload.scale())));
    }
}
