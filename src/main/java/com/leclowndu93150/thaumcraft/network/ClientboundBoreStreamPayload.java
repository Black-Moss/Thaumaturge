package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundBoreStreamPayload(
        double sx, double sy, double sz,
        int targetEntityId,
        int color,
        int count,
        float scale,
        int extend,
        double my
) implements CustomPacketPayload {

    public static final Type<ClientboundBoreStreamPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "bore_stream"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBoreStreamPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.sx);
                buf.writeDouble(data.sy);
                buf.writeDouble(data.sz);
                buf.writeVarInt(data.targetEntityId);
                buf.writeInt(data.color);
                buf.writeVarInt(data.count);
                buf.writeFloat(data.scale);
                buf.writeVarInt(data.extend);
                buf.writeDouble(data.my);
            },
            buf -> new ClientboundBoreStreamPayload(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readVarInt(),
                    buf.readInt(), buf.readVarInt(), buf.readFloat(), buf.readVarInt(), buf.readDouble()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
