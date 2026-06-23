package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumcraft.network.ClientboundAuraSnapshotPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public final class AuraSnapshotClientHandler {
    private AuraSnapshotClientHandler() {}

    public static void handle(ClientboundAuraSnapshotPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAuraCache.put(
                new ChunkPos(payload.chunkX(), payload.chunkZ()),
                payload.base(),
                payload.vis(),
                payload.flux()
        ));
    }
}
