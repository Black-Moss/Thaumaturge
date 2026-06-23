package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundEssentiaStreamPayload(
        double sx, double sy, double sz,
        double tx, double ty, double tz,
        int color,
        int count,
        float scale,
        int extend,
        double my
) implements CustomPacketPayload {

    public static final Type<ClientboundEssentiaStreamPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "essentia_stream"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEssentiaStreamPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.sx);
                buf.writeDouble(data.sy);
                buf.writeDouble(data.sz);
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeInt(data.color);
                buf.writeVarInt(data.count);
                buf.writeFloat(data.scale);
                buf.writeVarInt(data.extend);
                buf.writeDouble(data.my);
            },
            buf -> new ClientboundEssentiaStreamPayload(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(), buf.readVarInt(), buf.readFloat(), buf.readVarInt(), buf.readDouble()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
