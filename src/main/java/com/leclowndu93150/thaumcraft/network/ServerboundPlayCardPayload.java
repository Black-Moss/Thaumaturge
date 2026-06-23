package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundPlayCardPayload(int choiceIndex) implements CustomPacketPayload {
    public static final Type<ServerboundPlayCardPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "play_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPlayCardPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ServerboundPlayCardPayload::choiceIndex,
                    ServerboundPlayCardPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
