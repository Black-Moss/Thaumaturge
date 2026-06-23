package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundAdvanceStagePayload(Identifier research) implements CustomPacketPayload {
    public static final Type<ServerboundAdvanceStagePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "advance_stage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAdvanceStagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC,
                    ServerboundAdvanceStagePayload::research,
                    ServerboundAdvanceStagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
