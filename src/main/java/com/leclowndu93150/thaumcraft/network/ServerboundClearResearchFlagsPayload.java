package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.capability.ResearchFlag;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundClearResearchFlagsPayload(Identifier research, List<ResearchFlag> flags) implements CustomPacketPayload {
    public static final Type<ServerboundClearResearchFlagsPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "clear_research_flags"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClearResearchFlagsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC,
                    ServerboundClearResearchFlagsPayload::research,
                    ResearchFlag.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ServerboundClearResearchFlagsPayload::flags,
                    ServerboundClearResearchFlagsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
