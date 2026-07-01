package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FluxGooDropletData(int color, float alpha, int lifetime) implements ParticleOptions {
    public static final MapCodec<FluxGooDropletData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.fieldOf("color").forGetter(FluxGooDropletData::color),
            Codec.FLOAT.fieldOf("alpha").forGetter(FluxGooDropletData::alpha),
            Codec.INT.fieldOf("lifetime").forGetter(FluxGooDropletData::lifetime)
    ).apply(inst, FluxGooDropletData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluxGooDropletData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FluxGooDropletData::color,
            ByteBufCodecs.FLOAT, FluxGooDropletData::alpha,
            ByteBufCodecs.VAR_INT, FluxGooDropletData::lifetime,
            FluxGooDropletData::new
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FLUX_GOO_DROPLET.get();
    }
}
