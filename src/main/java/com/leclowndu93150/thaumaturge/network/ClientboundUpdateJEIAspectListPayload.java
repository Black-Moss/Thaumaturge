package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.compat.jei.AspectJeiSync;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientboundUpdateJEIAspectListPayload implements CustomPacketPayload {
    public static final Type<ClientboundUpdateJEIAspectListPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(TCIds.MODID, "update_jei_aspect_list"));

    public static final ClientboundUpdateJEIAspectListPayload INSTANCE = new ClientboundUpdateJEIAspectListPayload();


    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateJEIAspectListPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ClientboundUpdateJEIAspectListPayload(){}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
