package com.leclowndu93150.thaumaturge.client.model.connected;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record ConnectedTexture(Identifier texture, Identifier sheet, Optional<Identifier> framed) {
    public static final MapCodec<ConnectedTexture> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Identifier.CODEC.fieldOf("texture").forGetter(ConnectedTexture::texture), Identifier.CODEC.fieldOf("sheet").forGetter(ConnectedTexture::sheet),
                    Identifier.CODEC.optionalFieldOf("framed").forGetter(ConnectedTexture::framed)).apply(instance, ConnectedTexture::new));
    public static final Codec<ConnectedTexture> CODEC = MAP_CODEC.codec();

    public Identifier unconnectedCorner() {
        return framed.orElse(texture);
    }
}
