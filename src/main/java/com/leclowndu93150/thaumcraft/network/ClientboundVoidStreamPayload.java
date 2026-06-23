package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundVoidStreamPayload(
        double sx, double sy, double sz,
        double tx, double ty, double tz,
        int seed,
        float scale
) implements CustomPacketPayload {

    public static final Type<ClientboundVoidStreamPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "void_stream"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundVoidStreamPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.sx);
                buf.writeDouble(data.sy);
                buf.writeDouble(data.sz);
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeVarInt(data.seed);
                buf.writeFloat(data.scale);
            },
            buf -> new ClientboundVoidStreamPayload(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readVarInt(), buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
