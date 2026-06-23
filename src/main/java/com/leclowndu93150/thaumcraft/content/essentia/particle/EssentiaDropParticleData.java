package com.leclowndu93150.thaumcraft.content.essentia.particle;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EssentiaDropParticleData(int color, float alpha) implements ParticleOptions {
    public static final MapCodec<EssentiaDropParticleData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(EssentiaDropParticleData::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(EssentiaDropParticleData::alpha)
            ).apply(instance, EssentiaDropParticleData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaDropParticleData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeInt(data.color);
                buf.writeFloat(data.alpha);
            },
            buf -> new EssentiaDropParticleData(buf.readInt(), buf.readFloat())
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.ESSENTIA_DROP.get();
    }
}
