package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundArcPayload(
        double sx, double sy, double sz,
        double tx, double ty, double tz,
        int color,
        float gravity
) implements CustomPacketPayload {

    public static final Type<ClientboundArcPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "arc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundArcPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.sx);
                buf.writeDouble(data.sy);
                buf.writeDouble(data.sz);
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeInt(data.color);
                buf.writeFloat(data.gravity);
            },
            buf -> new ClientboundArcPayload(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(), buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
