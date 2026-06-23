package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundEndSessionPayload() implements CustomPacketPayload {
    public static final ServerboundEndSessionPayload INSTANCE = new ServerboundEndSessionPayload();

    public static final Type<ServerboundEndSessionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "end_session"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEndSessionPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
