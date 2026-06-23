package com.leclowndu93150.thaumcraft.content.essentia;

import com.leclowndu93150.thaumcraft.api.essentia.EssentiaList;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class EssentiaContentsComponent {
    public static final Codec<EssentiaList> CODEC = EssentiaList.CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaList> STREAM_CODEC = EssentiaList.STREAM_CODEC;

    private EssentiaContentsComponent() {}
}
