package com.leclowndu93150.thaumcraft.network.fx;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundFXStreamPayload(
        FXStreamKind kind,
        double sx, double sy, double sz,
        double tx, double ty, double tz,
        int color,
        int extraInt,
        int extraInt2,
        float extraFloat,
        float extraFloat2,
        int entityId,
        byte flags
) implements CustomPacketPayload {

    public static final byte FLAG_REVERSE = 1;
    public static final byte FLAG_WITH_SOURCE = 2;

    public static final Type<ClientboundFXStreamPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "fx_stream"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFXStreamPayload> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeByte(data.kind.ordinal());
                buf.writeDouble(data.sx);
                buf.writeDouble(data.sy);
                buf.writeDouble(data.sz);
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeInt(data.color);
                buf.writeVarInt(data.extraInt);
                buf.writeVarInt(data.extraInt2);
                buf.writeFloat(data.extraFloat);
                buf.writeFloat(data.extraFloat2);
                buf.writeVarInt(data.entityId);
                buf.writeByte(data.flags);
            },
            buf -> new ClientboundFXStreamPayload(
                    FXStreamKind.byOrdinal(buf.readByte()),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(),
                    buf.readVarInt(), buf.readVarInt(),
                    buf.readFloat(), buf.readFloat(),
                    buf.readVarInt(),
                    buf.readByte()));

    public boolean hasFlag(byte mask) {
        return (this.flags & mask) != 0;
    }

    public static ClientboundFXStreamPayload arc(double sx, double sy, double sz, double tx, double ty, double tz, int color, float gravity) {
        return new ClientboundFXStreamPayload(FXStreamKind.ARC, sx, sy, sz, tx, ty, tz, color, 0, 0, gravity, 0F, -1, (byte) 0);
    }

    public static ClientboundFXStreamPayload bolt(double sx, double sy, double sz, double tx, double ty, double tz, int color, float width) {
        return new ClientboundFXStreamPayload(FXStreamKind.BOLT, sx, sy, sz, tx, ty, tz, color, 0, 0, width, 0F, -1, (byte) 0);
    }

    public static ClientboundFXStreamPayload beam(double sx, double sy, double sz, double tx, double ty, double tz,
                                                   int color, int age, int beamType, float endMod, boolean reverse,
                                                   int sourceEntityId, boolean withSource) {
        byte flags = 0;
        if (reverse) flags |= FLAG_REVERSE;
        if (withSource) flags |= FLAG_WITH_SOURCE;
        return new ClientboundFXStreamPayload(FXStreamKind.BEAM, sx, sy, sz, tx, ty, tz, color, age, beamType, endMod, 0F, sourceEntityId, flags);
    }

    public static ClientboundFXStreamPayload essentia(double sx, double sy, double sz, double tx, double ty, double tz,
                                                       int color, int count, float scale, int extend, double my) {
        return new ClientboundFXStreamPayload(FXStreamKind.ESSENTIA, sx, sy, sz, tx, ty, tz, color, count, extend, scale, (float) my, -1, (byte) 0);
    }

    public static ClientboundFXStreamPayload bore(double sx, double sy, double sz, int targetEntityId,
                                                   int color, int count, float scale, int extend, double my) {
        return new ClientboundFXStreamPayload(FXStreamKind.BORE, sx, sy, sz, 0, 0, 0, color, count, extend, scale, (float) my, targetEntityId, (byte) 0);
    }

    public static ClientboundFXStreamPayload voidStream(double sx, double sy, double sz, double tx, double ty, double tz, int seed, float scale) {
        return new ClientboundFXStreamPayload(FXStreamKind.VOID, sx, sy, sz, tx, ty, tz, 0, seed, 0, scale, 0F, -1, (byte) 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
