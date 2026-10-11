package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundClearResearchFlagsPayload(Identifier research, List<ResearchFlag> flags) implements CustomPacketPayload {
    public static final Type<ServerboundClearResearchFlagsPayload> TYPE = new Type<>(TTIds.rl("clear_research_flags"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClearResearchFlagsPayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC,
            ServerboundClearResearchFlagsPayload::research, ResearchFlag.STREAM_CODEC.apply(ByteBufCodecs.list(ResearchFlag.values().length)), ServerboundClearResearchFlagsPayload::flags,
            ServerboundClearResearchFlagsPayload::new);

    public static void handle(ServerboundClearResearchFlagsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            PlayerResearch knowledge = PlayerResearchAccess.of(player);
            if (!knowledge.knows(payload.research()))
                return;
            boolean changed = false;
            for (ResearchFlag flag : payload.flags()) {
                if (knowledge.removeFlag(payload.research(), flag)) {
                    changed = true;
                }
            }
            if (changed)
                knowledge.queueSync(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
