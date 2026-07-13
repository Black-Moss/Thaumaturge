package com.leclowndu93150.thaumcraft.content.research.share;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShareBinding(UUID player, String name) {
    public static final Codec<ShareBinding> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            UUIDUtil.CODEC.fieldOf("player").forGetter(ShareBinding::player),
            Codec.STRING.fieldOf("name").forGetter(ShareBinding::name)
    ).apply(builder, ShareBinding::new));

    public static final StreamCodec<ByteBuf, ShareBinding> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ShareBinding::player,
            ByteBufCodecs.STRING_UTF8, ShareBinding::name,
            ShareBinding::new);
}
