package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundBeamPayload(
        double sx, double sy, double sz,
        double tx, double ty, double tz,
        int color,
        int age,
        int beamType,
        float endMod,
        boolean reverse,
        int sourceEntityId,
        boolean withSource
) implements CustomPacketPayload {

    public static final Type<ClientboundBeamPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBeamPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.sx);
                buf.writeDouble(data.sy);
                buf.writeDouble(data.sz);
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeInt(data.color);
                buf.writeVarInt(data.age);
                buf.writeVarInt(data.beamType);
                buf.writeFloat(data.endMod);
                buf.writeBoolean(data.reverse);
                buf.writeVarInt(data.sourceEntityId);
                buf.writeBoolean(data.withSource);
            },
            buf -> new ClientboundBeamPayload(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readFloat(), buf.readBoolean(), buf.readVarInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
