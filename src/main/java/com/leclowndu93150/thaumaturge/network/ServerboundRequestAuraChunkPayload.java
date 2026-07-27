package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundRequestAuraChunkPayload(int chunkX, int chunkZ) implements CustomPacketPayload {
    public static final Type<ServerboundRequestAuraChunkPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "request_aura_chunk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestAuraChunkPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundRequestAuraChunkPayload::chunkX,
                    ByteBufCodecs.VAR_INT, ServerboundRequestAuraChunkPayload::chunkZ,
                    ServerboundRequestAuraChunkPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
