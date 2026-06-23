package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundCardAnimationCompletePayload() implements CustomPacketPayload {
    public static final ServerboundCardAnimationCompletePayload INSTANCE = new ServerboundCardAnimationCompletePayload();

    public static final Type<ServerboundCardAnimationCompletePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "card_animation_complete"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCardAnimationCompletePayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
