package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireMoteData(
        double vx, double vy, double vz,
        float r, float g, float b,
        float alpha,
        float scale,
        boolean translucent
) implements ParticleOptions {

    public static final MapCodec<FireMoteData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            com.mojang.serialization.Codec.DOUBLE.fieldOf("vx").forGetter(FireMoteData::vx),
            com.mojang.serialization.Codec.DOUBLE.fieldOf("vy").forGetter(FireMoteData::vy),
            com.mojang.serialization.Codec.DOUBLE.fieldOf("vz").forGetter(FireMoteData::vz),
            com.mojang.serialization.Codec.FLOAT.fieldOf("r").forGetter(FireMoteData::r),
            com.mojang.serialization.Codec.FLOAT.fieldOf("g").forGetter(FireMoteData::g),
            com.mojang.serialization.Codec.FLOAT.fieldOf("b").forGetter(FireMoteData::b),
            com.mojang.serialization.Codec.FLOAT.fieldOf("alpha").forGetter(FireMoteData::alpha),
            com.mojang.serialization.Codec.FLOAT.fieldOf("scale").forGetter(FireMoteData::scale),
            com.mojang.serialization.Codec.BOOL.fieldOf("translucent").forGetter(FireMoteData::translucent)
    ).apply(inst, FireMoteData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireMoteData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, FireMoteData::vx,
            ByteBufCodecs.DOUBLE, FireMoteData::vy,
            ByteBufCodecs.DOUBLE, FireMoteData::vz,
            ByteBufCodecs.FLOAT, FireMoteData::r,
            ByteBufCodecs.FLOAT, FireMoteData::g,
            ByteBufCodecs.FLOAT, FireMoteData::b,
            ByteBufCodecs.FLOAT, FireMoteData::alpha,
            ByteBufCodecs.FLOAT, FireMoteData::scale,
            ByteBufCodecs.BOOL, FireMoteData::translucent,
            FireMoteData::new
    );

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FIRE_MOTE.get();
    }
}
