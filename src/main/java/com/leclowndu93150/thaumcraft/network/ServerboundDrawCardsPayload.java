package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundDrawCardsPayload() implements CustomPacketPayload {
    public static final ServerboundDrawCardsPayload INSTANCE = new ServerboundDrawCardsPayload();

    public static final Type<ServerboundDrawCardsPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "draw_cards"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDrawCardsPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
