package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundThaumatoriumTogglePayload(BlockPos pos, Identifier recipeId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> TYPE =
            new CustomPacketPayload.Type<>(TCIds.rl("thaumatorium_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundThaumatoriumTogglePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ServerboundThaumatoriumTogglePayload::pos,
                    Identifier.STREAM_CODEC, ServerboundThaumatoriumTogglePayload::recipeId,
                    ServerboundThaumatoriumTogglePayload::new);

    @Override
    public CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> type() {
        return TYPE;
    }
}
