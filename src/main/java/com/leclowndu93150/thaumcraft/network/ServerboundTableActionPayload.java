package com.leclowndu93150.thaumcraft.network;

import com.leclowndu93150.thaumcraft.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundTableActionPayload(Action action) implements CustomPacketPayload {
    public static final Type<ServerboundTableActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "table_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTableActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(Action::byId, Action::id),
                    ServerboundTableActionPayload::action,
                    ServerboundTableActionPayload::new);

    public enum Action {
        START,
        COMPLETE,
        SCRAP;

        public int id() {
            return ordinal();
        }

        public static Action byId(int id) {
            return values()[id];
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
