package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;

public record ClientboundKnowledgeGainPayload(
        KnowledgeType knowledgeType,
        Optional<ResourceKey<IResearchCategory>> category
) implements CustomPacketPayload {
    public static final Type<ClientboundKnowledgeGainPayload> TYPE = new Type<>(TCIds.rl("knowledge_gain"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundKnowledgeGainPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(index -> KnowledgeType.values()[index], KnowledgeType::ordinal),
                    ClientboundKnowledgeGainPayload::knowledgeType,
                    ByteBufCodecs.optional(ResourceKey.streamCodec(IResearchCategory.REGISTRY_KEY)),
                    ClientboundKnowledgeGainPayload::category,
                    ClientboundKnowledgeGainPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
