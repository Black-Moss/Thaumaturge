package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import com.leclowndu93150.thaumcraft.registry.TCTheorycraft;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record ServerboundStartTheoryPayload(List<ResourceKey<ITheorycraftAid>> selectedAids) implements CustomPacketPayload {
    public static final Type<ServerboundStartTheoryPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "start_theory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartTheoryPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(TCTheorycraft.AIDS_REGISTRY_KEY).apply(ByteBufCodecs.list()),
                    ServerboundStartTheoryPayload::selectedAids,
                    ServerboundStartTheoryPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
